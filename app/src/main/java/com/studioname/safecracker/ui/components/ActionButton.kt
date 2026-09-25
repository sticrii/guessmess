package com.studioname.safecracker.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reusable action button component to enforce consistency. To be used as the main button on the screen
 *
 * @param onClick Callback function which is called upon pressing the button
 * @param buttonText Text to be displayed on the button
 * @param modifier Modifier to allow in-place modification
 */
@Composable
fun ActionButton(onClick: () -> Unit, buttonText: String, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFB83728)
        ),
        shape = RectangleShape,
        modifier = modifier.padding(top = 20.dp, start = 20.dp, end = 20.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 3.dp,
            disabledElevation = 0.dp
        )
    ) {
        Text(
            text = buttonText,
            fontSize = 30.sp,
            modifier = Modifier.padding(10.dp),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Color.White
        )
    }
}