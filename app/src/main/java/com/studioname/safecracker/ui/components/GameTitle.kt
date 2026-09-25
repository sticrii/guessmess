package com.studioname.safecracker.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reusable GameTitle component, will be replaced by game logo
 *
 * @param modifier Modifier to allow in-place modification. Default is only padding values: top = 200.dp, bottom = 30.dp
 */
@Composable
fun GameTitle(modifier: Modifier = Modifier.padding(top = 200.dp, bottom = 30.dp)) {

    Text(
        text = "SAFE CRACKER",
        textAlign = TextAlign.Center,
        fontSize = 40.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        letterSpacing = 3.sp,
        modifier = modifier
    )
}