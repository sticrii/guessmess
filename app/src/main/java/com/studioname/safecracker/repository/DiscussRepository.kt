package com.studioname.safecracker.repository

import com.studioname.safecracker.model.Player
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscussRepository @Inject constructor(
    private val mainRepository: MainRepository
) {
    fun getLobbyPlayers(lobbyCode: String): Flow<List<Player>> = mainRepository.getLobbyPlayers(lobbyCode)
}