package com.studioname.safecracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.studioname.safecracker.enums.ButtonColors
import com.studioname.safecracker.enums.TextSize
import com.studioname.safecracker.ui.components.ActionButton
import com.studioname.safecracker.ui.theme.SafeCrackerTheme
import com.studioname.safecracker.viewmodel.LobbyVM
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.studioname.safecracker.R
import com.studioname.safecracker.enums.StickerSize
import com.studioname.safecracker.ui.components.PlayerSticker
import com.studioname.safecracker.ui.components.StickerAndName

@Composable
fun LobbyScreen(
    viewModel: LobbyVM?,
    onLeave: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var errorMessage by remember { mutableStateOf("") }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .padding(24.dp)
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "HOST_NAME'S PARTY",
            color = Color.White,
            fontSize = TextSize.TITLE.value,
            lineHeight = 50.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 25.dp)
        )

        Text(
            text = "PARTY CODE: ${viewModel?.lobbyCode}",
            fontWeight = FontWeight.Medium,
            color = Color.White,
            fontSize = 20.sp
        )

        StickerAndName(size = StickerSize.SHOWCASE, displayName = "my_nickname")

        ActionButton(
            onClick = {}, // TODO: write ready logic
            buttonText = "READY",
            modifier = Modifier.width(250.dp)
        )

        Button(
            onClick = {
                viewModel?.leaveLobby(
                    onSuccess = onLeave,
                    onError = { errorMessage = it }
                ) ?: onLeave()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonColors.SECONDARY.value
            ),
            shape = RectangleShape,
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 3.dp,
                disabledElevation = 0.dp
            )
        ) {
            Text(
                text = "LEAVE",
                fontWeight = FontWeight.SemiBold,
                fontSize = TextSize.SUBTITLE.value,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(7.dp)
            )
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LobbyScreenPreview() {
    SafeCrackerTheme {
        LobbyScreen(
            viewModel = null,
            onLeave = {}
        )
    }
}
