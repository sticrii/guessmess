package com.studioname.safecracker.repository

import com.google.firebase.database.ServerValue
import com.studioname.safecracker.model.Player
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

    suspend fun leaveLobby(lobbyCode: String, playerId: String) {
        val lobbyReference = mainRepository.getLobby(lobbyCode)
        val playersReference = lobbyReference.child("players")

        val currentHost = mainRepository.getLobbyHost(lobbyCode).first()

        playersReference.child(playerId).removeValue().await()

        val playersSnapshot = playersReference.get().await()
        val nextHostId = playersSnapshot.children.firstOrNull()?.key

        if (nextHostId == null) {
            lobbyReference.child("emptySince").setValue(ServerValue.TIMESTAMP).await()
            return
        }

        if (currentHost.id.value == playerId) {
            lobbyReference.child("hostId").setValue(nextHostId).await()
        }
    }

    fun startGame(lobbyCode: String) {
        val lobbyReference = mainRepository.getLobby(lobbyCode)
        lobbyReference.child("phase").setValue("PICK_ROOM")
    }
}
