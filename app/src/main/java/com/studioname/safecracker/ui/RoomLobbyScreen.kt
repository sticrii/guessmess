package com.studioname.safecracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studioname.safecracker.ui.components.ActionButton
import com.studioname.safecracker.viewmodel.RoomLobbyVM
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

@Composable
fun RoomLobbyScreen(
    roomCode: String,
    viewModel: RoomLobbyVM,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier
) {
    var errorMessage by remember { mutableStateOf("") }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "LOBBY",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Room code: $roomCode",
            color = Color.White,
            fontSize = 20.sp
        )

        Text(
            text = "Players",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "Waiting for players...",
            color = Color.LightGray,
            modifier = Modifier.fillMaxWidth()
        )

        ActionButton(
            onClick = {
                viewModel.leaveLobby(
                    roomCode = roomCode,
                    onSuccess = onLeave,
                    onError = { errorMessage = it }
                )
            },
            buttonText = "LEAVE LOBBY",
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.White
            )
        }
    }
}