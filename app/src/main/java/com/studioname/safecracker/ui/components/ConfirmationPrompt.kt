package com.studioname.safecracker.ui.components

import android.security.ConfirmationPrompt
import androidx.compose.foundation.layout.Arrangement
import com.studioname.safecracker.R
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studioname.safecracker.enums.TextSize
import com.studioname.safecracker.ui.theme.SafeCrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationPrompt(
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = { onCancel() }
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = AlertDialogDefaults.TonalElevation,
            color = colorResource(id = R.color.cardBackground),
            shape = RectangleShape
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "LEAVING ALREADY?",
                    color = Color.White,
                    fontSize = TextSize.SUBTITLE.value,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp,
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 3.dp),
                    thickness = 2.dp,
                    color = Color.Gray
                )

                Text(
                    text = "If you leave your progress will be permanently lost.",
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontSize = TextSize.INFO.value,
                    fontWeight = FontWeight.Normal,
                )

                Row(
                    modifier = Modifier.padding(top = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SecondaryButton(
                        onClick = { onConfirm() },
                        buttonText = "LEAVE"
                    )

                    ActionButton(
                        onClick = { onCancel() },
                        buttonText = "STAY",
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF666666)
@Composable
fun ConfirmationPromptPreview() {
    SafeCrackerTheme() {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ConfirmationPrompt(
                onConfirm = {},
                onCancel = {}
            )
        }
    }
}