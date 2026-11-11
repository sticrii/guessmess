package com.studioname.safecracker.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.viewModelScope
import com.studioname.safecracker.model.Room
import com.studioname.safecracker.ui.components.ConfirmationPrompt
import com.studioname.safecracker.ui.components.PlayerSticker
import kotlinx.coroutines.launch
import kotlin.collections.emptyList
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.random.Random
import androidx.compose.ui.geometry.Offset
import com.studioname.safecracker.ui.components.TimerText


// number of rows and columns in the grid
private data class RoomGrid(val cols: Int, val rows: Int)


/**
 * Calculate the number of rows and columns in a grid given the count of rooms and
 * box size (width and height)
 */
private fun computeRoomGrid(count: Int, width: Dp, height: Dp): RoomGrid {
    if (count <= 0 || width <= 0.dp || height <= 0.dp) return RoomGrid(cols = 1, rows = 1)

    val aspectRatio = width.value / height.value
    val cols = kotlin.math.sqrt(count * aspectRatio)
        .roundToInt()
        .coerceIn(1, count)
    val rows = ceil(count / cols.toFloat()).toInt()

    return RoomGrid(cols, rows)
}

// data for storing jitter of each note
private data class NoteJitter(
    val offsetXFraction: Float,
    val offsetYFraction: Float,
    val rotationDegrees: Float,
)

/**
 * Maps t in [0, 1) to a fractional (x, y) position on the perimeter of the
 * unit square, starting at the bottom-left corner (t = 0) and going
 * bottom-left -> bottom-right -> top-right -> top-left -> back to start.
 */
private fun perimeterFraction(t: Float): Offset {
    val tt = t - kotlin.math.floor(t) // wrap into [0, 1)
    return when {
        tt < 0.25f -> Offset(tt / 0.25f, 1f)                     // bottom edge, L -> R
        tt < 0.5f  -> Offset(1f, 1f - (tt - 0.25f) / 0.25f)       // right edge, bottom -> top
        tt < 0.75f -> Offset(1f - (tt - 0.5f) / 0.25f, 0f)        // top edge, R -> L
        else       -> Offset(0f, (tt - 0.75f) / 0.25f)            // left edge, top -> bottom
    }
}

/** Fractional positions (relative to the note's bounds) for n players. */
private fun computePlayerStickerPositions(playerCount: Int): List<Offset> {
    if (playerCount <= 0) return emptyList()
    return List(playerCount) { i -> perimeterFraction(i.toFloat() / playerCount) }
}

// remember the jitters so it doesnt change upon change of screen
@Composable
private fun rememberNoteJitters(count: Int): List<NoteJitter> =
    remember(count) {
        List(count) {
            NoteJitter(
                offsetXFraction = Random.nextFloat() * 2f - 1f,
                offsetYFraction = Random.nextFloat() * 2f - 1f,
                rotationDegrees = Random.nextDouble(-8.0, 8.0).toFloat(),
            )
        }
    }

@Composable
fun PickRoomScreen(
    viewModel: PickRoomVM? = null,
    navigateToRoom: (String) -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPrompt by remember { mutableStateOf(false) }
    val rooms = viewModel?.rooms

    BackHandler(enabled = true) {
        showPrompt = true
    }

    val timeRemaining by (viewModel?.timeRemaining ?: MutableStateFlow(15)).collectAsStateWithLifecycle()
    var boardSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current // for converting the px to dp

    val numberOfRooms = rooms?.size ?: 5

    // when destination room is set, we will call navigate to room
    LaunchedEffect(viewModel?.destinationRoom) {
        viewModel?.destinationRoom?.let { navigateToRoom(it) }
    }

    val playerRoomsMap by (viewModel?.playerRooms ?: MutableStateFlow(emptyMap())).collectAsStateWithLifecycle()
    val playersNotPicked = viewModel?.players?.filter {
        !playerRoomsMap.containsKey(it.id.value)
    } ?: emptyList()

    if (showPrompt) {
        ConfirmationPrompt(
            onConfirm = {
                viewModel?.leaveLobby(
                    onSuccess = onLeave,
                    onError = {}
                ) ?: onLeave()
                showPrompt = false
            },
            onCancel = { showPrompt = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .safeDrawingPadding()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        TitleText(text = "PICK A ROOM")

        TimerText(timeRemaining = timeRemaining)

        // displays all players in a container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val playersList = viewModel?.players
            if (playersList.isNullOrEmpty()) {
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
                    playersNotPicked.forEach { player ->
                        PlayerSticker(size = StickerSize.SMALL)
                    }
                }
            }
        }

        // the board
        Box(
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxSize()
                .background(color = Color(0xFFF6F6F4))
                .clipToBounds()
                .onGloballyPositioned { coordinates ->
                    boardSize = coordinates.size
                },
        ) {
            // store dimensions in dp
            val boardWidth: Dp = with(density) { boardSize.width.toDp() }
            val boardHeight: Dp = with(density) { boardSize.height.toDp() }

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
                        transformOrigin = TransformOrigin(
                            pivotFractionX = 0.2f,
                            pivotFractionY = 0.8f
                        )
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

            // dynamically calculated placement of notes
            if (boardWidth > 0.dp && boardHeight > 0.dp) {
                // padding from the edge of the board so notes don't touch it
                // top gets more space because of the divider bars at the top of the board
                val boardPadding = 10.dp
                val topPadding = 40.dp
                val usableWidth = (boardWidth - boardPadding * 2).coerceAtLeast(0.dp)
                val usableHeight = (boardHeight - topPadding - boardPadding).coerceAtLeast(0.dp)

                val grid = remember(numberOfRooms, usableWidth, usableHeight) {
                    computeRoomGrid(numberOfRooms, usableWidth, usableHeight)
                }
                val jitters = rememberNoteJitters(numberOfRooms)

                val cellWidth = usableWidth / grid.cols
                val cellHeight = usableHeight / grid.rows

                // note size respective of number of notes and space
                val noteSize = (minOf(cellWidth, cellHeight) * 0.80f).coerceIn(90.dp, 130.dp)

                val maxOffsetX = ((cellWidth - noteSize) / 2f).coerceAtLeast(0.dp) * 0.6f
                val maxOffsetY = ((cellHeight - noteSize) / 2f).coerceAtLeast(0.dp) * 0.6f

                rooms?.let {
                    for ((i, room) in rooms.withIndex()) {
                        val row = i / grid.cols
                        val col = i % grid.cols

                        // used for centering if the row is not full
                        val itemsInThisRow = if (row == grid.rows - 1) {
                            numberOfRooms - grid.cols * (grid.rows - 1)
                        } else {
                            grid.cols
                        }
                        val rowWidth = cellWidth * itemsInThisRow
                        val rowStartX = boardPadding + (usableWidth - rowWidth) / 2f

                        val cellX = rowStartX + cellWidth * col
                        val cellY = topPadding + cellHeight * row

                        val baseX = cellX + (cellWidth - noteSize) / 2f
                        val baseY = cellY + (cellHeight - noteSize) / 2f

                        val jitter = jitters[i]
                        val finalX = baseX + maxOffsetX * jitter.offsetXFraction
                        val finalY = baseY + maxOffsetY * jitter.offsetYFraction

                        val playersInRoom = viewModel.getRoomPlayers(room.name)
                        println("[ROOM]: Players in ${room.name}: $playersInRoom")
                        val stickerFractions = remember(playersInRoom.size) {
                            computePlayerStickerPositions(playersInRoom.size)
                        }

                        Box(
                            modifier = Modifier
                                .offset(x = finalX, y = finalY)
                                .size(noteSize)
                                .rotate(jitter.rotationDegrees)
                        ) {
                            RoomNote(
                                roomName = room.name,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { viewModel.pickRoom(room.name) },
                                referenceImage = R.drawable.room
                            )

                            playersInRoom.forEachIndexed { index, player ->
                                val frac = stickerFractions[index]
                                println("[SUCCESS]: Added ${player.displayName} to the room")
                                PlayerSticker(
                                    size = StickerSize.SMALL,
                                    modifier = Modifier.offset(
                                        x = noteSize * frac.x - StickerSize.SMALL.value / 2,
                                        y = noteSize * frac.y - StickerSize.SMALL.value / 2
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PickRoomScreenPreview() {
    SafeCrackerTheme {
        PickRoomScreen(
            navigateToRoom = {},
            onLeave = {},
        )
    }
}