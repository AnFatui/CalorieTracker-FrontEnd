package com.example.calorietracker.ui.theme.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calorietracker.ui.theme.AppUiState
import com.example.calorietracker.ui.theme.components.AppBottomBar

@Composable
fun WaterTrackingScreen(
    appState: AppUiState,
    onStateChange: (AppUiState) -> Unit,
    onBackClick: () -> Unit
) {
    val progress =
        if (appState.waterGoalMl <= 0 || appState.waterMl <= 0) {
            0f
        } else {
            appState.waterMl.toFloat() / appState.waterGoalMl.toFloat()
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(42.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "←",
                color = Color.White,
                fontSize = 30.sp,
                modifier = Modifier.clickable { onBackClick() }
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = "Wasser",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))

        Box(contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(160.dp)) {
                drawArc(
                    color = Color(0xFF1F2937),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(16.dp.toPx(), cap = StrokeCap.Round)
                )

                if (progress > 0f) {
                    drawArc(
                        color = Color(0xFF3B82F6),
                        startAngle = -90f,
                        sweepAngle = 360f * progress.coerceIn(0f, 1f),
                        useCenter = false,
                        style = Stroke(16.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("💧", fontSize = 30.sp)

                Text(
                    text = "${String.format("%.1f", appState.waterMl / 1000.0)} L",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "/ ${String.format("%.1f", appState.waterGoalMl / 1000.0)} L",
                    color = Color.Gray,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WaterButton("-") {
                onStateChange(
                    appState.copy(
                        waterMl = (appState.waterMl - 250).coerceAtLeast(0)
                    )
                )
            }

            Text(
                text = "Ziel: ${String.format("%.1f", appState.waterGoalMl / 1000.0)} Liter",
                color = Color.White
            )

            WaterButton("+") {
                onStateChange(
                    appState.copy(
                        waterMl = appState.waterMl + 250
                    )
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1F2937), RoundedCornerShape(14.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Erinnerung", color = Color.White)
            Text("Alle 60 min >", color = Color.White)
        }

        Spacer(Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .background(Color(0xFF1F2937), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Text(
                text = "Verlauf",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(14.dp))

            if (appState.waterMl == 0) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Noch kein Wasser eingetragen.",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(105.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.Bottom
                ) {
                    WaterDayBar("Mo", 0)
                    WaterDayBar("Di", 0)
                    WaterDayBar("Mi", 0)
                    WaterDayBar("Do", 0)
                    WaterDayBar("Fr", 0)
                    WaterDayBar("Sa", 0)
                    WaterDayBar("So", appState.waterMl)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        AppBottomBar(
            selected = "Statistics",
            onHomeClick = onBackClick,
            onMealsClick = {},
            onAddClick = {},
            onStatisticsClick = {},
            onProfileClick = {}
        )
    }
}

@Composable
private fun WaterButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .background(Color(0xFF1F2937), RoundedCornerShape(14.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun WaterDayBar(
    day: String,
    amountMl: Int
) {
    val maxMl = 2500
    val progress =
        if (amountMl <= 0) {
            0f
        } else {
            amountMl.toFloat() / maxMl.toFloat()
        }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .width(18.dp)
                .height(72.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            if (progress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(progress.coerceIn(0f, 1f))
                        .background(Color(0xFF3B82F6), RoundedCornerShape(8.dp))
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = day,
            color = Color.White,
            fontSize = 10.sp
        )
    }
}