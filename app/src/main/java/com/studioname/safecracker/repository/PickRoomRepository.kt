package com.studioname.safecracker.repository

import com.studioname.safecracker.R
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.studioname.safecracker.model.Hint
import com.studioname.safecracker.model.Player
import com.studioname.safecracker.model.PlayerId
import com.studioname.safecracker.model.Room
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import com.google.firebase.Firebase
import com.google.firebase.database.database
import kotlinx.coroutines.launch

@Singleton
class PickRoomRepository @Inject constructor(
    private val mainRepository: MainRepository
) {
    // used only because of .getValue() function, it cant take in <List<Player>> directly
    private val playersInType = object : GenericTypeIndicator<Map<String, Boolean>>() {}
    private val hintsType = object : GenericTypeIndicator<Map<String, Boolean>>() {}
    suspend fun getCurrentPlayerId(): String = mainRepository.getCurrentPlayerId()

    suspend fun getPlayer(playerId: String): Player? = mainRepository.getPlayer(PlayerId(playerId))

    fun getLobbyPlayers(lobbyCode: String): Flow<List<Player>> = mainRepository.getLobbyPlayers(lobbyCode)

    suspend fun leaveLobby(lobbyCode: String, playerId: String) = mainRepository.leaveLobby(lobbyCode, playerId)

    fun getRoomsData(lobbyCode: String): Flow<List<Room>> = callbackFlow {
        val lobbyReference = mainRepository.getLobby(lobbyCode)
        val ref = lobbyReference.child("rooms")

        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rooms = mutableListOf<Room>()
                for (child in snapshot.children) {
                    val roomName = child.key ?: continue
                    val playersInMap = child.child("playersIn").getValue(playersInType) ?: emptyMap()
                    val playersIn = playersInMap.keys.toList()
                    val referenceImage = child.child("referenceImage").getValue(Int::class.java) ?: R.drawable.room
                    val hintIdsMap = child.child("hintIds").getValue(hintsType) ?: emptyMap()
                    val hintIds = hintIdsMap.keys.toList()

                    val room = Room(
                        name = roomName,
                        playersIn = playersIn,
                        referenceImage = referenceImage,
                        hintIds = hintIds
                    )
                    rooms.add(room)
                }
                trySend(rooms)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getPlayerRooms(lobbyCode: String): Flow<Map<String, String>> = callbackFlow {
        val lobbyReference = mainRepository.getLobby(lobbyCode)
        val ref = lobbyReference.child("roomChoices")

        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val roomChoices = mutableMapOf<String, String>()
                for (child in snapshot.children) {
                    val playerId = child.key ?: continue
                    val roomName = child.getValue(String::class.java) ?: continue
                    roomChoices[playerId] = roomName
                }
                trySend(roomChoices)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun setPlayerRoom(lobbyCode: String, playerId: PlayerId, roomName: String) {
        val lobbyReference = mainRepository.getLobby(lobbyCode)
        val roomRef = lobbyReference.child("roomChoices").child(playerId.value)
        val currentRoom = roomRef.get().await().getValue(String::class.java) ?: ""

        if (currentRoom.isNotEmpty()) {
            lobbyReference.child("rooms").child(currentRoom).child("playersIn").child(playerId.value).removeValue()
            roomRef.removeValue()
        }

        lobbyReference.child("roomChoices").child(playerId.value).setValue(roomName)
        lobbyReference.child("/rooms/$roomName/playersIn").child(playerId.value).setValue(true)
        println("[ROOM]: $playerId picked $roomName")
    }


    suspend fun isHost(lobbyCode: String, playerId: String): Boolean {
        val hostId = mainRepository
            .getLobby(lobbyCode)
            .child("hostId")
            .get()
            .await()
            .getValue(String::class.java)

        return hostId == playerId
    }

    suspend fun changePhase(
        lobbyCode: String,
        newPhase: String = "PICK_HINT"
    ) {
        mainRepository
            .getLobby(lobbyCode)
            .child("phase")
            .setValue(newPhase)
            .await()
    }


    fun getPhaseStartTime(lobbyCode: String): Flow<Long?> = callbackFlow {
        val ref = mainRepository
            .getLobby(lobbyCode)
            .child("phaseStartTime")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Long::class.java))
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }


    fun getRoomHints(lobbyCode: String, roomName: String): Flow<List<Hint>> {
        val roomKey = roomName.trim().lowercase().replace(" ", "_")
        require(roomKey.matches(Regex("^[a-z0-9_]+$"))) {
            "Room name can only contain lowercase letters, numbers, and underscores."
        }

        return callbackFlow {
            val assignedIdsReference = mainRepository
                .getLobby(lobbyCode)
                .child("rooms")
                .child(roomKey)
                .child("hintIds")

            val sourceHintsReference = Firebase.database.reference
                .child("hints")
                .child(roomKey)

            var assignedIds: Set<String> = emptySet()
            var sourceHints: Map<String, Hint> = emptyMap()

            fun publishHints() {
                trySend(assignedIds.mapNotNull { id -> sourceHints[id] })
            }

            val idsListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    assignedIds = snapshot.children.mapNotNull { it.key }.toSet()
                    publishHints()
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }

            val hintsListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    sourceHints = snapshot.children.mapNotNull { hintSnapshot ->
                        val id = hintSnapshot.key ?: return@mapNotNull null
                        val type = hintSnapshot.child("type")
                            .getValue(String::class.java) ?: return@mapNotNull null
                        val content = hintSnapshot.child("content")
                            .getValue(String::class.java) ?: return@mapNotNull null

                        try {
                            id to Hint(id = id, roomKey = roomKey, type = type, content = content)
                        } catch (exception: IllegalArgumentException) {
                            null
                        }
                    }.toMap()

                    publishHints()
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }

            assignedIdsReference.addValueEventListener(idsListener)
            sourceHintsReference.addValueEventListener(hintsListener)

            awaitClose {
                assignedIdsReference.removeEventListener(idsListener)
                sourceHintsReference.removeEventListener(hintsListener)
            }
        }
    }

    fun getPlayerHints(lobbyCode: String, playerId: PlayerId): Flow<List<Hint>> =
        callbackFlow {
            val hintIdsReference = mainRepository
                .getLobby(lobbyCode)
                .child("players")
                .child(playerId.value)
                .child("hintIds")

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    launch {
                        try {
                            val playerHints = mutableListOf<Hint>()

                            for (roomSnapshot in snapshot.children) {
                                val roomKey = roomSnapshot.key ?: continue
                                val hintIds = roomSnapshot.children.mapNotNull { it.key }.toSet()

                                val sourceSnapshot = Firebase.database.reference
                                    .child("hints")
                                    .child(roomKey)
                                    .get()
                                    .await()

                                for (hintSnapshot in sourceSnapshot.children) {
                                    val hintId = hintSnapshot.key ?: continue
                                    if (hintId !in hintIds) continue

                                    val type = hintSnapshot.child("type")
                                        .getValue(String::class.java) ?: continue
                                    val content = hintSnapshot.child("content")
                                        .getValue(String::class.java) ?: continue

                                    try {
                                        playerHints.add(
                                            Hint(id = hintId, roomKey = roomKey, type = type, content = content)
                                        )
                                    } catch (exception: IllegalArgumentException) {
                                        // Preskoči neispravan hint.
                                    }
                                }
                            }

                            trySend(playerHints)
                        } catch (exception: Exception) {
                            close(exception)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }

            hintIdsReference.addValueEventListener(listener)
            awaitClose {
                hintIdsReference.removeEventListener(listener)
            }
        }

    suspend fun addHintToPlayer(
        lobbyCode: String,
        playerId: PlayerId,
        roomKey: String,
        hintId: String
    ) {
        mainRepository
            .getLobby(lobbyCode)
            .child("players")
            .child(playerId.value)
            .child("hintIds")
            .child(roomKey)
            .child(hintId)
            .setValue(true)
            .await()
    }

}
