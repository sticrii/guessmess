package com.studioname.safecracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studioname.safecracker.ui.components.ActionButton
import com.studioname.safecracker.ui.components.GameTitle
import com.studioname.safecracker.ui.theme.SafeCrackerTheme
import com.studioname.safecracker.viewmodel.MainMenuVM

@Composable
fun MainMenuScreen(
    viewModel: MainMenuVM?,
    onLobbyJoined: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var lobbyCode by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
            .background(color = Color.DarkGray).padding(15.dp)
    ) {
        GameTitle(
            modifier = Modifier.padding(top = 120.dp, bottom = 80.dp)
        )

        ActionButton(
            onClick = {
                viewModel?.createLobby(
                    onSuccess = { code ->
                        onLobbyJoined(code)
                    },
                    onError = { error ->
                        message = error
                    }
                )
            },
            buttonText = "START PARTY",
            modifier = Modifier.fillMaxWidth().padding(20.dp)
        )
        if (message.isNotEmpty()) {
            Text(
                text = message,
                color = Color.White
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight().padding(bottom = 100.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = "Already have a code?",
                fontSize = 25.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            TextField(
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,

                    focusedContainerColor = Color.Gray,
                    unfocusedContainerColor = Color.LightGray,
                ),
                value = lobbyCode,
                onValueChange = { input ->
                    lobbyCode = input
                        .uppercase()
                        .filter { it in "23456789EHKNRWXYZ" }
                        .take(6)
                },
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp)
            )

            ActionButton(
                onClick = {
                    val code = lobbyCode.trim().uppercase()

                    viewModel?.joinLobby(
                        lobbyCode = code,
                        onSuccess = {
                            onLobbyJoined(code)
                        },
                        onError = { error ->
                            message = error
                        }
                    )
                },
                buttonText = "JOIN PARTY",
                modifier = Modifier.fillMaxWidth().padding(20.dp)
            )
        }
    }
}

@Preview
@Composable
fun MainMenuPreview() {
    SafeCrackerTheme {
        MainMenuScreen(viewModel = null)
    }
}
