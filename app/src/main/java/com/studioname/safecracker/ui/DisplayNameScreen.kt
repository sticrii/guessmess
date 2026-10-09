package com.studioname.safecracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.studioname.safecracker.enums.TextSize
import com.studioname.safecracker.ui.components.ActionButton
import com.studioname.safecracker.ui.components.GameTitle
import com.studioname.safecracker.ui.theme.SafeCrackerTheme
import com.studioname.safecracker.viewmodel.DisplayNameVM
import androidx.compose.runtime.collectAsState

/**
 * Represents the screen where the user is tasked with entering their nickname (purely cosmetic)
 *
 * @param modifier Modifier to allow in-place modification
 * @param navController NavController to allow navigation instructions to the next screen
 */
@Composable
fun DisplayNameScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    viewModel: DisplayNameVM? = null
) {
    val savedDisplayName = viewModel
        ?.savedDisplayName
        ?.collectAsState(initial = "")
        ?.value
        .orEmpty()

    var displayName by remember(savedDisplayName) { mutableStateOf(savedDisplayName) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
            .background(color = Color.DarkGray).padding(15.dp)
    ) {
        GameTitle()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight()
        ) {
            Text(
                text = "Enter a nickname",
                fontSize = TextSize.SUBTITLE.value,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(20.dp)
            )

            TextField(
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.Gray,
                    unfocusedContainerColor = Color.LightGray,
                ),
                value = displayName,
                onValueChange = { displayName = it },
            )

            ActionButton(
                onClick = {
                    viewModel?.onNameConfirmed(displayName)

                    navController?.navigate("main_menu") {
                        popUpTo("display_name") { inclusive = true }
                    }
                },
                buttonText = "LET'S GO",
                modifier = Modifier.padding(20.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DNScreenPreview() {
    SafeCrackerTheme {
        DisplayNameScreen()
    }
}