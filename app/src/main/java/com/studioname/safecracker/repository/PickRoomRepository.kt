package com.studioname.safecracker.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.studioname.safecracker.model.Hint
import com.studioname.safecracker.model.Player
import com.studioname.safecracker.model.PlayerId
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PickRoomRepository @Inject constructor(
    private val mainRepository: MainRepository
) {
    suspend fun getCurrentPlayerId(): String = mainRepository.getCurrentPlayerId()

    fun getLobbyPlayers(lobbyCode: String): Flow<List<Player>> = mainRepository.getLobbyPlayers(lobbyCode)

    fun getPlayerRooms(lobbyCode: String): Flow<Map<String, String>> = callbackFlow {
        val lobbyReference = mainRepository.getLobby(lobbyCode)
        val ref = lobbyReference.child("roomChoices")

        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val roomChoices = mutableMapOf<String, String>()
                for (child in snapshot.children) {
                    val playerId = child.key ?: continue
                    val roomId = child.getValue(String::class.java) ?: continue
                    roomChoices[playerId] = roomId
                }
                trySend(roomChoices)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    fun setPlayerRoom(lobbyCode: String, playerId: PlayerId, roomName: String) {
        val lobbyReference = mainRepository.getLobby(lobbyCode)
        lobbyReference.child("roomChoices").child(playerId.value).setValue(roomName)
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
