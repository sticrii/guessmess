package com.studioname.safecracker.ui.components

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
import com.studioname.safecracker.enums.ButtonColors
import com.studioname.safecracker.enums.TextSize

@Composable
fun SecondaryButton(
    onClick: () -> Unit,
    buttonText: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = ButtonColors.SECONDARY.value
        ),
        shape = RectangleShape,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 3.dp,
            disabledElevation = 0.dp
        ),
        modifier = modifier
    ) {
        Text(
            text = buttonText,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = TextSize.SUBTITLE.value,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(vertical = 5.dp)
        )
    }
}