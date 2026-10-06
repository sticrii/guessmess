package com.studioname.safecracker.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.studioname.safecracker.model.Player
import kotlinx.coroutines.flow.MutableStateFlow

class PickRoomVM(

) {
    var players by mutableStateOf<List<Player>>(emptyList())
        private set
    val initialTime = 15 // seconds

    // MutableStateFlow - a thread-safe data holder and hot stream in Kotlin that stores a single,
    // current value and shares it with multiple observers
    var timeRemaining = MutableStateFlow(initialTime)
        private set

}