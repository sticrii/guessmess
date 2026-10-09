package com.studioname.safecracker.repository

import com.google.firebase.Firebase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.studioname.safecracker.model.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainMenuRepository @Inject constructor(
    private val mainRepository: MainRepository
) {
    private val db = Firebase.database.reference

    val savedDisplayName: Flow<String> = mainRepository.savedDisplayName

    suspend fun saveDisplayName(name: String) = mainRepository.saveDisplayName(name)

    suspend fun getCurrentPlayerId(): String = mainRepository.getCurrentPlayerId()

    suspend fun getCurrentPlayer(): Player? = mainRepository.getCurrentPlayer()

    suspend fun setPlayer(player: Player) = mainRepository.setPlayer(player)

    private fun createPlayerData(player: Player): Map<String, Any> {
        return mapOf(
            "name" to player.displayName.value,
            "failedAttempts" to 0,
            "isEliminated" to false
        )
    }

    suspend fun createLobby(player: Player): String {
        var lobbyCode: String

        do {
            lobbyCode = (1..MainRepository.LOBBY_CODE_LENGTH)
                .map { MainRepository.LOBBY_CODE_CHARS.random() }
                .joinToString("")

            val lobbyExists = db.child("lobbies").child(lobbyCode).get().await().exists()
        } while (lobbyExists)

        val lobby = mapOf(
            "phase" to "LOBBY",
            "round" to 1,
            "hostId" to player.id.value,
            "players" to mapOf(
                player.id.value to createPlayerData(player)
            )
        )
        val lobbyReference = db.child("lobbies").child(lobbyCode)
        val playerReference = lobbyReference.child("players").child(player.id.value)

        playerReference.onDisconnect().removeValue().await()
        lobbyReference.setValue(lobby).await()
        return lobbyCode
    }

    suspend fun joinLobby(lobbyCode: String, player: Player) {
        val lobbyReference = mainRepository.getLobby(lobbyCode)
        val lobbySnapshot = lobbyReference.get().await()

        if (!lobbySnapshot.exists()) {
            error("A lobby with that code doesn't exist.")
        }

        val phase = lobbySnapshot.child("phase").getValue(String::class.java)

        if (phase != "LOBBY") {
            error("This lobby is no longer accepting players.")
        }

        lobbyReference.child("players").child(player.id.value).onDisconnect().removeValue().await()

        lobbyReference.updateChildren(
            mapOf(
                "players/${player.id.value}" to createPlayerData(player),
                "emptySince" to null
            )
        ).await()
    }

    suspend fun cleanupExpiredEmptyLobbies() {
        val lobbiesReference = db.child("lobbies")
        val lobbiesSnapshot = lobbiesReference.get().await()
        val now = System.currentTimeMillis()
        val timeoutMillis = 15 * 60 * 1000L

        lobbiesSnapshot.children.forEach { lobby ->
            val lobbyCode = lobby.key ?: return@forEach

            if (lobby.child("players").hasChildren()) return@forEach

            val lobbyReference = lobbiesReference.child(lobbyCode)
            val emptySince = lobby.child("emptySince").getValue(Long::class.java)

            if (emptySince == null) {
                lobbyReference.child("emptySince").setValue(ServerValue.TIMESTAMP).await()
                return@forEach
            }

            if (now - emptySince >= timeoutMillis) {
                val currentLobby = lobbyReference.get().await()
                val currentEmptySince = currentLobby.child("emptySince").getValue(Long::class.java)

                if (!currentLobby.child("players").hasChildren() && currentEmptySince == emptySince) {
                    lobbyReference.removeValue().await()
                }
            }
        }
    }
}
