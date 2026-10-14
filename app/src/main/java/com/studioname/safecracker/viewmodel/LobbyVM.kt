package com.studioname.safecracker.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studioname.safecracker.model.Player
import com.studioname.safecracker.model.Room
import com.studioname.safecracker.repository.LobbyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LobbyVM @Inject constructor(
    private val repository: LobbyRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val lobbyCode: String = checkNotNull(savedStateHandle["lobbyCode"])
    var players by mutableStateOf<List<Player>>(emptyList())
        private set

    private var currentPlayerId: String? = null

    var host by mutableStateOf<Player?>(null)
        private set

    var gameStarted by mutableStateOf(false)
        private set

    init {
        // we call this at the start to get the current player id
        viewModelScope.launch {
            currentPlayerId = repository.getCurrentPlayerId()
        }

        // update all players
        viewModelScope.launch {
            repository.getLobbyPlayers(lobbyCode).collect { updatedPlayers ->
                players = updatedPlayers
            }
        }

        // get current host
        viewModelScope.launch {
            repository.getLobbyHost(lobbyCode).collect { updatedHost ->
                host = updatedHost
            }
        }

        // listen for game start
        viewModelScope.launch {
            repository.getLobbyPhase(lobbyCode).collect { phase ->
                if (phase == "STARTED") gameStarted = true
            }
        }
    }

    // save the current player
    val currentPlayer: Player?
        get() = players.find { it.id.value == currentPlayerId }

    fun leaveLobby(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val playerId = repository.getCurrentPlayerId()
                repository.leaveLobby(lobbyCode, playerId)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Couldn't leave lobby.")
            }
        }
    }

    fun startGame() {
        viewModelScope.launch {
            repository.startGame(
                lobbyCode = lobbyCode,
                rooms = listOf( // TODO: send list of rooms dynamically from the settings
                    Room(name = "Kitchen"),
                    Room(name = "Storage"),
                    Room(name = "Living Room"),
                    Room(name = "Art Room"),
                    Room(name = "Bathroom")
                )
            )
        }
    }
}
