package com.studioname.safecracker.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studioname.safecracker.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainMenuVM @Inject constructor(
    private val repository: Repository
    ):ViewModel(){
    fun createLobby(
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ){
        viewModelScope.launch {
            try {
                val player = repository.getCurrentPlayer()
                if(player == null){
                    onError("Player not found.")
                    return@launch
                }
                val roomCode = repository.createLobby(player)
                onSuccess(roomCode)
            }catch (e: Exception){
                onError(e.message ?: "Lobby couldn't be created.")
            }
        }
    }
    }