package com.example.calorietracker.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TrackingCard(
    title: String,
    value: String,
    icon: String,
    accentColor: Color,
    progress: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .height(92.dp)
            .background(Color(0xFF1F2937), RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row {
            Text(icon, fontSize = 24.sp)

            Spacer(Modifier.width(8.dp))

            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Text(
                    text = value,
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.sp
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .background(Color(0xFF374151), RoundedCornerShape(99.dp))
        ) {
            if (progress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(5.dp)
                        .background(accentColor, RoundedCornerShape(99.dp))
                )
            }
        }
    }
}