package com.studioname.safecracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.studioname.safecracker.R
import com.studioname.safecracker.enums.StickerSize
import com.studioname.safecracker.enums.TextSize
import com.studioname.safecracker.ui.theme.SafeCrackerTheme

@Composable
fun StickerAndName(size: StickerSize, referenceImage: Int = R.drawable.player_sticker, displayName: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (size == StickerSize.SHOWCASE) 10.dp else 5.dp),
    ) {
        PlayerSticker(size = size, referenceImage = referenceImage)

        Text(
            text = displayName,
            fontSize = if (size == StickerSize.SHOWCASE) TextSize.SUBTITLE.value else TextSize.INFO.value,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF444444)
@Composable
fun StickerAndNamePreview() {
    SafeCrackerTheme {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            StickerAndName(
                size = StickerSize.SHOWCASE,
                displayName = "Player 1"
            )
            StickerAndName(
                size = StickerSize.REGULAR,
                displayName = "Player 2"
            )
        }
    }
}