package com.studioname.safecracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.studioname.safecracker.R
import com.studioname.safecracker.enums.StickerSize

@Composable
fun PlayerSticker(
    size: StickerSize,
    referenceImage: Int = R.drawable.player_sticker,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        modifier = modifier.size(size.value).clip(CircleShape),
        model = referenceImage, // TODO: replace default with automatic sticker creation
        contentDescription = "Player sticker",
        contentScale = ContentScale.Fit,
    )
}