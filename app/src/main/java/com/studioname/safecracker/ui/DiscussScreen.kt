package com.studioname.safecracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.studioname.safecracker.enums.StickerSize
import com.studioname.safecracker.model.DisplayName
import com.studioname.safecracker.model.Player
import com.studioname.safecracker.model.PlayerId
import com.studioname.safecracker.ui.components.PlayerSticker
import com.studioname.safecracker.ui.components.TitleText
import com.studioname.safecracker.ui.theme.SafeCrackerTheme
import com.studioname.safecracker.viewmodel.DiscussVM
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studioname.safecracker.ui.components.ActionButton
import com.studioname.safecracker.ui.components.TimerText
import kotlinx.coroutines.flow.MutableStateFlow

private val fakePlayers = List(5) { index ->
    Player(
        id = PlayerId("fake_player_$index"),
        displayName = DisplayName("Player${index + 1}")
    )
}

@Composable
fun DiscussScreen(
    modifier: Modifier = Modifier,
    viewModel: DiscussVM? = null
) {
    val players = viewModel?.players ?: fakePlayers
    val stickerSize = if (players.size >= 9) StickerSize.REGULAR else if (players.size >= 6) StickerSize.LARGE else StickerSize.SHOWCASE

    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    val timeRemaining by (viewModel?.timeRemaining ?: MutableStateFlow(60)).collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxSize().background(color = Color.DarkGray).padding(top = 25.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TitleText(text = "DISCUSS!")

        TimerText(timeRemaining = timeRemaining, modifier = Modifier.padding(vertical = 20.dp))

        Box(
            modifier = Modifier
                .padding(top = 32.dp)
                .fillMaxWidth()
                .height(512.dp)
                .onGloballyPositioned { coordinates ->
                    boxSize = coordinates.size
                },
        ) {
            val boxWidth = with(density) { boxSize.width.toDp() }
            val boxHeight = with(density) { boxSize.height.toDp() }

            if (boxWidth > 0.dp && boxHeight > 0.dp) {
                val radiusX = (boxWidth - stickerSize.value) / 2
                val radiusY = (boxHeight - stickerSize.value) / 2
                val centerX = boxWidth / 2
                val centerY = boxHeight / 2

                players.forEachIndexed { index, player ->
                    val angleRad = Math.toRadians(-90.0 + index * (360.0 / players.size))
                    val x = centerX + radiusX * cos(angleRad).toFloat() - stickerSize.value / 2
                    val y = centerY + radiusY * sin(angleRad).toFloat() - stickerSize.value / 2

                    PlayerSticker(
                        size = stickerSize,
                        modifier = Modifier
                            .offset(x = x, y = y)
                            .clickable {
                                // TODO: create a list of available players for trading
                                // TODO: from which we will be picking players, and to whom
                                // TODO: the shake animation will apply

                                viewModel?.toggleSelectPlayer(player.id.value)
                            }
                    )
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            ActionButton(
                onClick = {
                    viewModel?.selectedPlayerId?.let {
                        viewModel.sendTradeRequest()
                    }
                },
                buttonText = "TRADE",
                modifier = Modifier.width(200.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DiscussScreenPreview() {
    SafeCrackerTheme {
        DiscussScreen()
    }
}