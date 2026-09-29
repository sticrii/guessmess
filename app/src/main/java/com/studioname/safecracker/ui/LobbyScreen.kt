package com.studioname.safecracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.studioname.safecracker.enums.ButtonColors
import com.studioname.safecracker.enums.TextSize
import com.studioname.safecracker.ui.components.ActionButton
import com.studioname.safecracker.ui.theme.SafeCrackerTheme
import com.studioname.safecracker.viewmodel.LobbyVM
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.studioname.safecracker.R
import com.studioname.safecracker.enums.StickerSize
import com.studioname.safecracker.ui.components.PlayerSticker
import com.studioname.safecracker.ui.components.StickerAndName

@Composable
fun LobbyScreen(
    viewModel: LobbyVM?,
    onLeave: () -> Unit = {},
    onStart: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var errorMessage by remember { mutableStateOf("") }
    val isHost = viewModel?.currentPlayer?.id?.value == viewModel?.host?.id?.value

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "${viewModel?.host?.displayName?.value?.uppercase() ?: "HOST_NAME"}'S PARTY",
            color = Color.White,
            fontSize = TextSize.TITLE.value,
            lineHeight = 50.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "PARTY CODE: ${viewModel?.lobbyCode ?: "123456"}",
            fontWeight = FontWeight.Medium,
            color = Color.White,
            fontSize = 20.sp
        )

        StickerAndName(
            size = StickerSize.SHOWCASE,
            displayName = viewModel?.currentPlayer?.displayName?.value ?: "my_nickname",
            // TODO: once done, change ViewModel? to ViewModel in LobbyScreen parameters
        )

        // display all other players, wraps if too many to display
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (viewModel?.players.isNullOrEmpty() || viewModel.players.size == 1) {
                Box(
                    modifier = Modifier.fillMaxSize().border(width = 1.dp, color = Color.Black),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Nobody seems to be here...",
                        color = Color.Gray,
                        fontSize = TextSize.INFO.value
                    )
                }
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    viewModel.players.forEach { player ->
                        if (player.displayName.value != viewModel.currentPlayer?.displayName?.value)
                            StickerAndName(size = StickerSize.REGULAR, displayName = player.displayName.value)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp) // Gap between buttons
        ) {
            Button(
                onClick = {
                    viewModel?.leaveLobby(
                        onSuccess = onLeave,
                        onError = { errorMessage = it }
                    ) ?: onLeave()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonColors.SECONDARY.value
                ),
                shape = RectangleShape,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 3.dp,
                    disabledElevation = 0.dp
                ),
                modifier = Modifier.weight(0.40f) // 35% of the space
            ) {
                Text(
                    text = "LEAVE",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = TextSize.SUBTITLE.value,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 5.dp)
                )
            }

            Button(
                onClick = { onStart() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonColors.ACTION.value
                ),
                shape = RectangleShape,
                modifier = modifier.weight(0.60f),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 3.dp,
                    disabledElevation = 0.dp
                ),
                enabled = isHost
            ) {
                Text(
                    text = "START",
                    fontSize = TextSize.ACTION.value,
                    modifier = Modifier.padding(10.dp),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
            }

        }


        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LobbyScreenPreview() {
    SafeCrackerTheme {
        LobbyScreen(
            viewModel = null,
            onLeave = {}
        )
    }
}
