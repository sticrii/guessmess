package com.studioname.safecracker.repository

import com.google.firebase.database.ServerValue
import com.studioname.safecracker.model.Player
import com.studioname.safecracker.model.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.studioname.safecracker.usecase.HintGenerator
import kotlin.random.Random

@Singleton
class LobbyRepository @Inject constructor(
    private val mainRepository: MainRepository,
    private val hintGenerator: HintGenerator
) {
    suspend fun getCurrentPlayerId(): String = mainRepository.getCurrentPlayerId()

    fun getLobbyPlayers(lobbyCode: String): Flow<List<Player>> = mainRepository.getLobbyPlayers(lobbyCode)

    fun getLobbyHost(lobbyCode: String): Flow<Player> = mainRepository.getLobbyHost(lobbyCode)

    fun getLobbyPhase(lobbyCode: String): Flow<String?> = mainRepository.getLobbyPhase(lobbyCode)

    suspend fun leaveLobby(lobbyCode: String, playerId: String) = mainRepository.leaveLobby(lobbyCode, playerId)

    suspend fun startGame(lobbyCode: String, rooms: List<Room>) {
        require(rooms.isNotEmpty()) {
            "At least one room is required to start the game."
        }

        val lobbyReference = mainRepository.getLobby(lobbyCode)

        val roomKeys = rooms.map { room ->
            room.name
                .trim()
                .lowercase()
                .replace(" ", "_")
        }

        val safeCode = Random.nextInt(from = 0, until = 1_000_000)
            .toString()
            .padStart(6, '0')

        val generatedHints = hintGenerator.generate(
            safeCode = safeCode,
            roomKeys = roomKeys
        )

        val updates = mutableMapOf<String, Any>(
            "phase" to "PICK_ROOM",
            "phaseStartTime" to ServerValue.TIMESTAMP,
            "safeCode" to safeCode
        )

        generatedHints.forEach { hint ->
            updates["hints/${hint.id}"] = mapOf(
                "templateId" to hint.templateId,
                "type" to hint.type.name,
                "roomKey" to hint.roomKey,
                "content" to hint.content
            )
        }

        rooms.forEachIndexed { index, room ->
            val roomKey = roomKeys[index]
            val roomHintIds = generatedHints
                .filter { it.roomKey == roomKey }
                .map { it.id }

            updates["rooms/$roomKey"] = mapOf(
                "hintIds" to roomHintIds.associateWith { true },
                "playersIn" to room.playersIn,
                "referenceImage" to room.referenceImage
            )
        }

        lobbyReference.updateChildren(updates).await()
    }
}
