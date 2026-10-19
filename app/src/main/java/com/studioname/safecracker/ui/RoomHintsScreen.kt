package com.studioname.safecracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studioname.safecracker.ui.components.TitleText
import com.studioname.safecracker.viewmodel.RoomHintsVM

@Composable
fun RoomHintsScreen(
    viewModel: RoomHintsVM,
    modifier: Modifier = Modifier
) {
    val roomKey = viewModel.roomName
    val ownedIds = viewModel.playerHints
        .filter { it.roomKey == roomKey }
        .map { it.id }
        .toSet()

    val availableHints = viewModel.roomHints.filterNot { it.id in ownedIds }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TitleText(text = "${roomKey.replace('_', ' ').uppercase()} HINTS")

        viewModel.errorMessage?.let { error ->
            Text(text = error, color = Color.Red)
        }

        if (availableHints.isEmpty()) {
            Text(
                text = "No more hints available in this room.",
                color = Color.White
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = availableHints,
                    key = { hint -> "${hint.roomKey}_${hint.id}" }
                ) { hint ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = hint.type,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(text = hint.content)

                            Button(
                                onClick = { viewModel.takeHint(hint.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("TAKE HINT")
                            }
                        }
                    }
                }
            }
        }
    }
}