package com.studioname.safecracker.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studioname.safecracker.enums.StickerSize
import com.studioname.safecracker.enums.TextSize
import com.studioname.safecracker.ui.components.StickerAndName
import com.studioname.safecracker.ui.components.TitleText
import com.studioname.safecracker.ui.theme.SafeCrackerTheme
import com.studioname.safecracker.viewmodel.PickRoomVM
import kotlinx.coroutines.flow.MutableStateFlow
import com.studioname.safecracker.R
import com.studioname.safecracker.ui.components.RoomNote

@Composable
fun PickRoomScreen (
    modifier: Modifier = Modifier,
    viewModel: PickRoomVM? = null,
) {
    val timeRemaining by (viewModel?.timeRemaining ?: MutableStateFlow(15)).collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .safeDrawingPadding()
            .padding(top= 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        TitleText(text = "PICK A ROOM")

        Text(
            text = "$timeRemaining seconds remaining",
            color = Color.White,
            fontSize = TextSize.SUBTITLE.value,
            fontWeight = FontWeight.Medium
        )

        // displays all players in a container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val playersList = viewModel?.players
            if (playersList.isNullOrEmpty() || playersList.size == 1) {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
                        StickerAndName(size = StickerSize.SMALL, displayName = player.displayName.value)
                    }
                }
            }
        }

        // the board
        Box(
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxSize()
                .background(color = Color(0xFFf6f6f4))
                .clipToBounds(),
        ) {
            // whiteboard background
            Image(
                painter = painterResource(id = R.drawable.whiteboard),
                contentDescription = "Whiteboard texture",
                contentScale = ContentScale.None,
                modifier = Modifier
                    .padding(top = 100.dp)
                    .size(400.dp)
                    .graphicsLayer(
                        scaleX = 3.5f,
                        scaleY = 3.5f,
                        transformOrigin = TransformOrigin(pivotFractionX = 0.2f, pivotFractionY = 0.8f)
                    )
            )

            // simulates the top of the whiteboard
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier.shadow(elevation = 2.5.dp)
            ) {
                HorizontalDivider(
                    thickness = 15.dp,
                    color = Color(0xFFCCCCCC)
                )
                HorizontalDivider(
                    thickness = 10.dp,
                    color = Color(0xFFE0E0E0)
                )
            }

            // rooms
            RoomNote(
                roomName = "Kitchen",
                modifier = Modifier
                    .size(120.dp)
                    .offset(x = 20.dp, y = 50.dp)
                    .clickable {} // gives us a square click target
                    .rotate(-10f)
            )

            RoomNote(
                roomName = "Kitchen",
                modifier = Modifier
                    .size(120.dp)
                    .offset(x = 240.dp, y = 50.dp)
                    .clickable {} // gives us a square click target
                    .rotate(20f)
            )

            RoomNote(
                roomName = "Kitchen",
                modifier = Modifier
                    .size(120.dp)
                    .offset(x = 80.dp, y = 190.dp)
                    .clickable {} // gives us a square click target
                    .rotate(-5f)
            )

            RoomNote(
                roomName = "Kitchen",
                modifier = Modifier
                    .size(120.dp)
                    .offset(x = 230.dp, y = 210.dp)
                    .clickable {} // gives us a square click target
                    .rotate(3f)
            )

            RoomNote(
                roomName = "Kitchen",
                modifier = Modifier
                    .size(120.dp)
                    .offset(x = 15.dp, y = 340.dp)
                    .clickable {} // gives us a square click target
                    .rotate(-8f)
            )

            RoomNote(
                roomName = "Kitchen",
                modifier = Modifier
                    .size(120.dp)
                    .offset(x = 180.dp, y = 340.dp)
                    .clickable {} // gives us a square click target
                    .rotate(8f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PickRoomScreenPreview() {
    SafeCrackerTheme {
        PickRoomScreen()
    }
}