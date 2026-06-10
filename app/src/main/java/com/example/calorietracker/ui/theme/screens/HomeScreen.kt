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
import com.example.calorietracker.ui.theme.components.MacroCircle
import com.example.calorietracker.ui.theme.components.TrackingCard

@Composable
fun HomeScreen(
    appState: AppUiState,
    onMealsClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onWaterClick: () -> Unit,
    onWeightClick: () -> Unit
) {
    val calorieProgress =
        if (appState.calorieGoal == 0) 0f else appState.calories.toFloat() / appState.calorieGoal.toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(38.dp))

        Text(
            text = if (appState.name.isBlank()) "Hallo! 👋" else "Hallo ${appState.name}! 👋",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text("Heute", color = Color.Gray, fontSize = 12.sp)

        Spacer(Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp)
                .background(Color(0xFF1F2937), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.size(86.dp)) {
                drawArc(
                    color = Color(0xFF4B5563),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(9.dp.toPx(), cap = StrokeCap.Round)
                )

                if (appState.calories > 0) {
                    drawArc(
                        color = Color(0xFF22C55E),
                        startAngle = -90f,
                        sweepAngle = 360f * calorieProgress.coerceIn(0f, 1f),
                        useCenter = false,
                        style = Stroke(9.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${appState.calories}",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "/ ${appState.calorieGoal} kcal",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            Text(
                text = "Kalorien",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(16.dp))

        Text("Makronährstoffe", color = Color.White, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(122.dp)
                .background(Color(0xFF1F2937), RoundedCornerShape(16.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MacroCircle(
                title = "Protein",
                value = "${appState.protein}",
                subtitle = "${appState.protein} / ${appState.proteinGoal} g",
                color = Color(0xFFEF4444),
                progress = safeProgress(appState.protein, appState.proteinGoal)
            )

            MacroCircle(
                title = "Kohlenhydrate",
                value = "${appState.carbs}",
                subtitle = "${appState.carbs} / ${appState.carbsGoal} g",
                color = Color(0xFFF97316),
                progress = safeProgress(appState.carbs, appState.carbsGoal)
            )

            MacroCircle(
                title = "Fette",
                value = "${appState.fat}",
                subtitle = "${appState.fat} / ${appState.fatGoal} g",
                color = Color(0xFFFACC15),
                progress = safeProgress(appState.fat, appState.fatGoal)
            )
        }

        Spacer(Modifier.height(22.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TrackingCard(
                title = "Wasser",
                value = "${formatLiter(appState.waterMl)} / ${formatLiter(appState.waterGoalMl)} L",
                icon = "💧",
                accentColor = Color(0xFF3B82F6),
                progress = safeProgress(appState.waterMl, appState.waterGoalMl),
                modifier = Modifier.weight(1f),
                onClick = onWaterClick
            )

            TrackingCard(
                title = "Schritte",
                value = "${appState.steps} / ${appState.stepGoal}",
                icon = "👣",
                accentColor = Color(0xFFA855F7),
                progress = safeProgress(appState.steps, appState.stepGoal),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(78.dp)
                .background(Color(0xFF1F2937), RoundedCornerShape(16.dp))
                .clickable { onWeightClick() }
                .padding(12.dp)
        ) {
            Text("Gewicht", color = Color.White, fontWeight = FontWeight.Bold)

            Text(
                text = "Aktuell: ${if (appState.currentWeight.isBlank()) "-" else appState.currentWeight + " kg"}   Ziel: ${if (appState.targetWeight.isBlank()) "-" else appState.targetWeight + " kg"}",
                color = Color.White,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterStart)
            )

            Text(">", color = Color.White, modifier = Modifier.align(Alignment.TopEnd))
        }

        Spacer(Modifier.weight(1f))

        AppBottomBar(
            selected = "Home",
            onHomeClick = {},
            onMealsClick = onMealsClick,
            onAddClick = onMealsClick,
            onStatisticsClick = onStatisticsClick,
            onProfileClick = onProfileClick
        )
    }
}

private fun safeProgress(value: Int, goal: Int): Float {
    if (goal <= 0 || value <= 0) return 0f
    return (value.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
}

private fun formatLiter(ml: Int): String {
    return String.format("%.1f", ml / 1000.0)
}