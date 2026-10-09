package com.studioname.safecracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studioname.safecracker.model.DisplayName
import com.studioname.safecracker.model.Player
import com.studioname.safecracker.model.PlayerId
import com.studioname.safecracker.repository.MainMenuRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for DisplayNameScreen
 *
 * @param repository MainMenuRepository to allow saving player display name
 */
@HiltViewModel
class DisplayNameVM @Inject constructor(
    private val repository: MainMenuRepository,
) : ViewModel() {

    val savedDisplayName: Flow<String> = repository.savedDisplayName

    fun onNameConfirmed(displayName: String) {
        viewModelScope.launch {
            repository.saveDisplayName(displayName)
            val playerId = repository.getCurrentPlayerId()
            val player = Player(
                id = PlayerId(playerId),
                displayName = DisplayName(displayName)
            )

            repository.setPlayer(player)
        }
    }
}
