package com.studioname.safecracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studioname.safecracker.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LobbyVM @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    fun leaveLobby(
        lobbyCode: String,
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
