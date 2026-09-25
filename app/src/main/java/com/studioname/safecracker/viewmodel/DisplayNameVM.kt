package com.studioname.safecracker.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studioname.safecracker.model.DisplayName
import com.studioname.safecracker.model.Player
import com.studioname.safecracker.model.PlayerId
import com.studioname.safecracker.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class DisplayNameVM @Inject constructor(
    private val repository: Repository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    fun onNameConfirmed(displayName: String) {
        viewModelScope.launch {
            try {
                val playerId = repository.getCurrentPlayerId()

                // locally store the displayName
                dataStore.edit { it[stringPreferencesKey("displayName")] = displayName }

                val player = Player(
                    id = PlayerId(playerId),
                    displayName = DisplayName(displayName)
                )

                repository.setPlayer(player)
            } catch (e: Exception) {
                println("[ERROR]: Failed to confirm name: ${e.message}")
            }
        }
    }
}