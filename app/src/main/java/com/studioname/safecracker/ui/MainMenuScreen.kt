package com.studioname.safecracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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

@Composable
fun MainMenuScreen(modifier: Modifier = Modifier) {
    var roomCode by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
            .background(color = Color.DarkGray).padding(15.dp)
    ) {
        GameTitle(
            modifier = Modifier.padding(top = 120.dp, bottom = 80.dp)
        )

        ActionButton(
            onClick = {},
            buttonText = "CREATE LOBBY",
            modifier = Modifier.fillMaxWidth()
        )

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
                value = roomCode,
                onValueChange = { roomCode = it },
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp)
            )

            ActionButton(
                onClick = {},
                buttonText = "JOIN LOBBY",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview
@Composable
fun MainMenuPreview() {
    SafeCrackerTheme() {
        MainMenuScreen()
    }
}