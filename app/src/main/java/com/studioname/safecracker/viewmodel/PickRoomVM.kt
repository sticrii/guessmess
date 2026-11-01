package com.studioname.safecracker.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.constraintlayout.helper.widget.Flow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studioname.safecracker.model.Player
import com.studioname.safecracker.model.PlayerId
import com.studioname.safecracker.model.Room
import com.studioname.safecracker.repository.PickRoomRepository
import com.studioname.safecracker.usecase.CountdownUC
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PickRoomVM @Inject constructor(
    private val repository: PickRoomRepository,
    private val countdown: CountdownUC,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {
    val lobbyCode: String = checkNotNull(savedStateHandle["lobbyCode"])
    var players by mutableStateOf<List<Player>>(emptyList())
        private set

    private fun getPlayerById(id: PlayerId): Player? {
        for (player in players) {
            if (player.id.value == id.value) {
                return player
            }
        }

        return null
    }

    val playersNotPicked: List<Player>
        get() = players.filter { player -> !playerRooms.value.containsKey(player.id.value) }

    var rooms by mutableStateOf<List<Room>>(emptyList())
        private set

    private var currentPlayerId: String? = null
    val initialTime = 60 // seconds TODO: replace with the value you get from firebase phaseStartTime

    private val _timeRemaining = MutableStateFlow(initialTime)
    val timeRemaining: StateFlow<Int> = _timeRemaining.asStateFlow()

    private val _playerRooms = MutableStateFlow<Map<String, String>>(emptyMap())
    val playerRooms: StateFlow<Map<String, String>> = _playerRooms.asStateFlow()

    var destinationRoom by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            repository.getRoomsData(lobbyCode).collect {
                rooms = it
                println("[ROOMS]: $rooms")
            }
        }

        // set current player
        viewModelScope.launch {
            currentPlayerId = repository.getCurrentPlayerId()
        }

        // update players
        viewModelScope.launch {
            repository.getLobbyPlayers(lobbyCode).collect {
                players = it
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

        // update player room choices
        viewModelScope.launch {
            repository.getPlayerRooms(lobbyCode).collect { playerRooms ->
                _playerRooms.value = playerRooms
            }
        }
    }

    fun onTimeExpire() {
        try {
            val playerRoom = playerRooms.value.get(currentPlayerId)

            repository.changePhase(lobbyCode)
            destinationRoom = playerRoom ?: "test"
        } catch (e: Exception) {
            println("[ERROR]: Player ID not found ${e.message}")
        }
    }

    fun pickRoom(roomName: String) {
        viewModelScope.launch {
            currentPlayerId?.let {
                repository.setPlayerRoom(lobbyCode, PlayerId(it), roomName)
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

    fun getRoomPlayers(roomName: String): List<Player> {
        val players: MutableList<Player> = mutableListOf()
        for (room in rooms) {
            if (room.name == roomName) {
                for (playerId in room.playersIn) {
                    val player = getPlayerById(PlayerId(playerId))

                    player?.let { players.add(player) }
                }
            }
        }

        return players
    }
}
