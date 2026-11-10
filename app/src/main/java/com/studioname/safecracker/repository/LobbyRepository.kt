package com.studioname.safecracker.repository

import com.google.firebase.database.ServerValue
import com.studioname.safecracker.model.Player
import com.studioname.safecracker.model.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LobbyRepository @Inject constructor(
    private val mainRepository: MainRepository
) {
    suspend fun getCurrentPlayerId(): String = mainRepository.getCurrentPlayerId()

    fun getLobbyPlayers(lobbyCode: String): Flow<List<Player>> = mainRepository.getLobbyPlayers(lobbyCode)

    fun getLobbyHost(lobbyCode: String): Flow<Player> = mainRepository.getLobbyHost(lobbyCode)

    fun getLobbyPhase(lobbyCode: String): Flow<String?> = mainRepository.getLobbyPhase(lobbyCode)

    suspend fun leaveLobby(lobbyCode: String, playerId: String) = mainRepository.leaveLobby(lobbyCode, playerId)

    suspend fun startGame(lobbyCode: String, rooms: List<Room>) {
        val lobbyReference = mainRepository.getLobby(lobbyCode)

        val updates = mutableMapOf<String, Any>(
            "phase" to "PICK_ROOM",
            "phaseStartTime" to ServerValue.TIMESTAMP
        )
        for (room in rooms) {
            val roomKey = room.name
                .trim()
                .lowercase()
                .replace(" ", "_")

            val hintIds = mainRepository.getRoomHintIds(roomKey)

            updates["rooms/$roomKey"] = mapOf(
                "hintIds" to hintIds.associateWith { true },
                "playersIn" to room.playersIn,
                "referenceImage" to room.referenceImage
            )
        }

        lobbyReference.updateChildren(updates).await()
    }
}
