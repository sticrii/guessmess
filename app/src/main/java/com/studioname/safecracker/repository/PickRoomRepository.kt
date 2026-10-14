package com.studioname.safecracker.repository

import android.R
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
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
                    val referenceImage = child.child("referenceImage").getValue(Int::class.java) ?: continue
                    val hintsMap = child.child("hints").getValue(hintsType) ?: emptyMap()
                    val hints = hintsMap.keys.toList()

                    val room = Room(
                        name = roomName,
                        playersIn = playersIn,
                        referenceImage = referenceImage,
                        hints = hints
                    )
                    rooms.add(room)
                }
                trySend(rooms)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    /*
    /**
     * @deprecated Use {@link #getRoomsData(lobbyCode)} instead
     */
    fun getRoomData(lobbyCode: String, roomName: String): Flow<Room> = callbackFlow {
        val lobbyReference = mainRepository.getLobby(lobbyCode)
        val ref = lobbyReference.child("rooms/$roomName")

        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val playersIn = snapshot.child("playersIn").getValue(playersInType) ?: emptyList()
                val referenceImage = snapshot.child("referenceImage").getValue(Int::class.java) ?: 0
                val hints = snapshot.child("hints").getValue(hintsType) ?: emptyList()

                val room = Room(
                    name = roomName,
                    playersIn = playersIn,
                    referenceImage = referenceImage,
                    hints = hints
                )

                trySend(room)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    /**
     * @deprecated Use {@link #getRoomsData(lobbyCode)} instead
     */
    fun getRoomPlayers(lobbyCode: String, roomName: String): Flow<List<Player>> = callbackFlow {
        val lobbyReference = mainRepository.getLobby(lobbyCode)
        val ref = lobbyReference.child("rooms/$roomName")

        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val playersIn = snapshot.child("playersIn").getValue(playersInType) ?: emptyList()

                trySend(playersIn)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        awaitClose { ref.removeEventListener(listener) }
    }
     */

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

    fun changePhase(lobbyCode: String, newPhase: String = "PICK_HINT") {
        val lobbyReference = mainRepository.getLobby(lobbyCode)
        lobbyReference.child("phase").setValue(newPhase)
    }

    fun getRoomHints(lobbyCode: String, roomName: String): Flow<List<Hint>> {
        val roomKey = roomName.trim().lowercase().replace(" ", "_")
        require(roomKey.matches(Regex("^[a-z0-9_]+$"))) {
            "Room name can only contain lowercase letters, numbers, and underscores."
        }
        val hintsReference = mainRepository.getLobby(lobbyCode).child("rooms").child(roomKey).child("hints")
        return observeHints(hintsReference)
    }

    fun getPlayerHints(lobbyCode: String, playerId: PlayerId): Flow<List<Hint>> {
        val hintsReference = mainRepository.getLobby(lobbyCode).child("players").child(playerId.value).child("hints")
        return observeHints(hintsReference)
    }

    private fun observeHints(hintsReference: DatabaseReference): Flow<List<Hint>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val hints = snapshot.children.mapNotNull { hintSnapshot ->
                    val hintId = hintSnapshot.key ?: return@mapNotNull null
                    val type = hintSnapshot.child("type").getValue(String::class.java) ?: return@mapNotNull null
                    val content = hintSnapshot.child("content").getValue(String::class.java) ?: return@mapNotNull null
                    Hint(id = hintId, type = type, content = content)
                }
                trySend(hints)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        hintsReference.addValueEventListener(listener)
        awaitClose { hintsReference.removeEventListener(listener) }
    }
}
