package com.studioname.safecracker.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ServerValue
import com.studioname.safecracker.enums.TradeState
import com.studioname.safecracker.model.Player
import com.studioname.safecracker.model.PlayerId
import com.studioname.safecracker.model.Trade
import com.studioname.safecracker.repository.DiscussRepository
import com.studioname.safecracker.usecase.CountdownUC
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.get
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class DiscussVM @Inject constructor(
    private val repository: DiscussRepository,
    private val countdown: CountdownUC,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val lobbyCode: String = checkNotNull(savedStateHandle["lobbyCode"])
    var players by mutableStateOf<List<Player>>(emptyList())
        private set

    var host by mutableStateOf<Player?>(null)
        private set

    private var currentPlayerId: String? = null

    var selectedPlayerId by mutableStateOf<String?>(null)
        private set

    private val _myTrades = MutableStateFlow<List<Trade>>(emptyList())

    val myTrades: StateFlow<List<Trade>> = _myTrades.asStateFlow()

    val pendingTrades: StateFlow<List<Trade>> = myTrades
        .map { trades -> trades.filter { it.state == TradeState.PENDING } }
        .stateIn( // map returns FLOW we need STATEFLOW, this is what changes to STATEFLOW, idk how it works
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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

        // update all my trades
        viewModelScope.launch {
            currentPlayerId?.let {
                repository.getMyTradeRequests(lobbyCode, it).collect { trades ->
                    _myTrades.value = trades
                }
            }
        }

        // get current host
        viewModelScope.launch {
            repository.getLobbyHost(lobbyCode).collect { updatedHost ->
                host = updatedHost
            }
        }

        // we call so we set all expired trades to declined
        if (host?.id?.value == currentPlayerId) {
            viewModelScope.launch {
                while (true) {
                    delay(1000.milliseconds)
                    repository.expireOldTrades(lobbyCode)
                }
            }
        }
    }

    fun onTimeExpire() {
        // only the host should clear the lobby
        if (host?.id?.value == currentPlayerId) {
            repository.deleteAllRequests(lobbyCode)
        }
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
            viewModelScope.launch {
                if (repository.getTradeBetween(
                        lobbyCode = lobbyCode,
                        id1 = selectedPlayerId!!,
                        id2 = currentPlayerId!!
                ) != null) {
                    println("[TRADES]: A pending trade between $selectedPlayerId and $currentPlayerId already exists!")
                    return@launch
                }

                repository.createRequest(
                    lobbyCode = lobbyCode,
                    receiverId = selectedPlayerId!!,
                    senderId = currentPlayerId!!
                )
            }

        }
    }

    fun getPlayerName(targetId: PlayerId): String {
        players.forEach { player ->
            if (player.id.value == targetId.value) {
                return player.displayName.value
            }
        }

        println("[ERROR]: Unable to find player with ID '${targetId.value}'")
        return "UnknownPlayer"
    }

    fun getTradeRemainingSecs(trade: Trade): Long {
        return System.currentTimeMillis() - trade.sentAt // TODO: move to more precise time management
    }
}