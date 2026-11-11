package com.studioname.safecracker.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.studioname.safecracker.enums.TextSize

@Composable
fun TimerText(timeRemaining: Int, modifier: Modifier = Modifier) {
    Text(
        text = "$timeRemaining seconds remaining",
        color = Color.White,
        fontSize = TextSize.SUBTITLE.value,
        fontWeight = FontWeight.Medium,
        modifier = modifier
    )
}