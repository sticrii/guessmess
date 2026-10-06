package com.studioname.safecracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.studioname.safecracker.R

@Composable
fun RoomNote(roomName: String, referenceImage: Int = R.drawable.room, modifier: Modifier) {
    AsyncImage(
        modifier = modifier.background(Color.Transparent),
        contentDescription = "Room",
        model = referenceImage,
        contentScale = ContentScale.Inside
    )
}