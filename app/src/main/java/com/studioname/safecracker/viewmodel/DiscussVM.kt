package com.studioname.safecracker.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studioname.safecracker.model.Player
import com.studioname.safecracker.model.PlayerId
import com.studioname.safecracker.repository.DiscussRepository
import com.studioname.safecracker.usecase.CountdownUC
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.get

@HiltViewModel
class DiscussVM @Inject constructor(
    private val repository: DiscussRepository,
    private val countdown: CountdownUC,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val lobbyCode: String = checkNotNull(savedStateHandle["lobbyCode"])
    var players by mutableStateOf<List<Player>>(emptyList())
        private set

    private var currentPlayerId: String? = null

    var selectedPlayerId by mutableStateOf<String?>(null)
        private set

    val initialTime = 60 // seconds TODO: replace with the value you get from firebase phaseStartTime

    private val _timeRemaining = MutableStateFlow(initialTime)
    val timeRemaining: StateFlow<Int> = _timeRemaining.asStateFlow()

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

        // update the timer and check for time expiring
        viewModelScope.launch {
            countdown(initialTime = initialTime).collect { secondsLeft ->
                _timeRemaining.value = secondsLeft

                if (secondsLeft == 0) {
                    onTimeExpire()
                }
            }
        }
    }

    fun onTimeExpire() {
        // TODO: implement
    }

    /**
     * Toggles between selected and deselected for target player, used for indicating which player
     * a trade is supposed to occur with, automatically deselects currently selected player if exists
     * @playerId ID of the target player
     */
    fun toggleSelectPlayer(playerId: String) {
        selectedPlayerId?.let {
            if (it == playerId) {
                selectedPlayerId = null
                return
            }
        }

        selectedPlayerId = playerId
    }

    fun sendTradeRequest() {
        if (selectedPlayerId != null && currentPlayerId != null) {
            repository.createRequest(
                lobbyCode = lobbyCode,
                receiverId = selectedPlayerId!!,
                senderId = currentPlayerId!!
            )
        }
    }
}