package com.studioname.safecracker.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studioname.safecracker.model.Player
import com.studioname.safecracker.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.collect

@HiltViewModel
class LobbyVM @Inject constructor(
    private val repository: Repository,
    private val savedStateHandle: SavedStateHandle  // for hilt, so we dont have to pass in code every time
) : ViewModel() {

    val lobbyCode: String = checkNotNull(savedStateHandle["lobbyCode"])
    var players by mutableStateOf<List<Player>>(emptyList())

    init {
        viewModelScope.launch {
            repository.getLobbyPlayers(lobbyCode).collect { players ->
                this@LobbyVM.players = players
            }
        }
    }

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

}
