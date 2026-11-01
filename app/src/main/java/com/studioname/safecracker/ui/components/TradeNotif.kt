package com.studioname.safecracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studioname.safecracker.R
import com.studioname.safecracker.enums.TextSize
import com.studioname.safecracker.ui.theme.SafeCrackerTheme
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.studioname.safecracker.enums.ButtonColors

@Composable
fun TradeNotif(
    modifier: Modifier = Modifier,
    senderName: String,
    timeRemaining: Float = 0.9f,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp)
            .fillMaxWidth()
            .height(100.dp)
            .shadow(elevation = 20.dp)
            .background(color = Color(0xFF282D30)),
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // replace with trade logo
            Icon(
                painter = painterResource(id = (R.drawable.trade_icon)),
                contentDescription = "Trade Icon",
                tint = Color.White,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(42.dp)
            )

            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 12.dp),
                thickness = 2.dp,
                color = Color.DarkGray
            )

            Column(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .fillMaxHeight()
                    .weight(0.80f),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "TRADE REQUEST!",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "${senderName.uppercase()} WANTS TO TRADE WITH YOU",
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp
                )
            }

            Column(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .padding(end = 12.dp)
                    .fillMaxHeight()
                    .weight(0.20f),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(R.drawable.accept),
                    contentDescription = "Accept",
                    modifier = Modifier
                        .size(34.dp)
                        .background(color = Color.Green)
                        .shadow(elevation = 12.dp),
                    tint = Color.White
                )

                Icon(
                    painter = painterResource(R.drawable.decline),
                    contentDescription = "Decline",
                    modifier = Modifier
                        .size(34.dp)
                        .background(color = Color.Red)
                        .shadow(elevation = 12.dp),
                    tint = Color.White
                )
            }
        }

        HorizontalDivider(
            color = ButtonColors.ACTION.value,
            modifier = Modifier.fillMaxWidth(fraction = timeRemaining),
            thickness = 5.dp
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF444444)
@Composable
fun TradeHeaderPreview() {
    SafeCrackerTheme {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TradeNotif(senderName = "12345678901234567890123")
        }
    }
}