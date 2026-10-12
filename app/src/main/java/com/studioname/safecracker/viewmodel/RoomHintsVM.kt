package com.studioname.safecracker.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studioname.safecracker.model.Hint
import com.studioname.safecracker.model.PlayerId
import com.studioname.safecracker.repository.PickRoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomHintsVM @Inject constructor(
    private val repository: PickRoomRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val lobbyCode: String =
        checkNotNull(savedStateHandle["lobbyCode"])

    val roomName: String =
        checkNotNull(savedStateHandle["roomName"])

    var roomHints by mutableStateOf<List<Hint>>(emptyList())
        private set

    var playerHints by mutableStateOf<List<Hint>>(emptyList())
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            try {
                val playerId = PlayerId(repository.getCurrentPlayerId())

                combine(
                    repository.getRoomHints(lobbyCode, roomName),
                    repository.getPlayerHints(lobbyCode, playerId)
                ) { availableHints, ownedHints ->
                    availableHints to ownedHints
                }.collect { (availableHints, ownedHints) ->
                    roomHints = availableHints
                    playerHints = ownedHints
                }
            } catch (exception: Exception) {
                errorMessage = exception.message ?: "Could not load hints."
            }
        }
    }
}