package com.example.calorietracker.ui.theme.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.calorietracker.ui.theme.components.MacroCircle
import com.example.calorietracker.ui.theme.components.TrackingCard
import com.example.calorietracker.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onWaterClick: () -> Unit,
    onWeightClick: () -> Unit,
    onFastingClick: () -> Unit,
    onStepsClick: () -> Unit,
    onSetTitle: (String) -> Unit,
    onSetLoading: (Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val calorieProgress = uiState.calorieGoal?.let { safeProgress(uiState.calories, it) }
    onSetTitle(uiState.displayName?.let { displayName -> "Hallo ${displayName}! 👋" } ?: "Hallo! 👋")

    LaunchedEffect(uiState.loading) {
        onSetLoading(uiState.loading)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text("Heute", color = Color.Gray, fontSize = 12.sp)
        }

        Spacer(Modifier.height(14.dp))

        // Fasten-Balken (Kompakt)
        uiState.fastingProgress?.let { progress ->
            val statusColor = if (uiState.isFasting) Color(0xFF22C55E) else Color(0xFF3B82F6) // Grün für Fasten, Blau für Essen

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFastingClick() }
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.isFasting) "Endet um ${uiState.fastingEndText}" else "Startet um ${uiState.fastingEndText}",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    Text(
                        text = uiState.fastingRemainingText ?: "",
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Color(0xFF1F2937), RoundedCornerShape(2.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .background(statusColor, RoundedCornerShape(2.dp))
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
                .background(Color(0xFF1F2937), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.size(90.dp)) {
                drawArc(
                    color = Color(0xFF374151),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(9.dp.toPx(), cap = StrokeCap.Round)
                )

                calorieProgress?.let {
                    if (it > 0f) {
                        drawArc(
                            color = Color(0xFF22C55E),
                            startAngle = -90f,
                            sweepAngle = 360f * it,
                            useCenter = false,
                            style = Stroke(9.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${uiState.calories}",
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("/ ${uiState.calorieGoal} kcal", color = Color.Gray, fontSize = 11.sp)
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

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(126.dp)
                .background(Color(0xFF1F2937), RoundedCornerShape(18.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MacroCircle(
                "Protein",
                "${uiState.protein}",
                "${uiState.protein} / ${uiState.proteinGoal} g",
                Color(0xFFEF4444),
                safeProgress(uiState.protein, uiState.proteinGoal ?: 0)
            )

            MacroCircle(
                "Carbs",
                "${uiState.carbs}",
                "${uiState.carbs} / ${uiState.carbsGoal} g",
                Color(0xFFF97316),
                safeProgress(uiState.carbs, uiState.carbsGoal ?: 0)
            )

            MacroCircle(
                "Fette",
                "${uiState.fat}",
                "${uiState.fat} / ${uiState.fatGoal} g",
                Color(0xFFFACC15),
                safeProgress(uiState.fat, uiState.fatGoal ?: 0)
            )
        }

        Spacer(Modifier.height(22.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TrackingCard(
                title = "Wasser",
                value = "${formatLiter(uiState.waterMl)} / ${formatLiter(uiState.waterGoalMl ?: 0)} L",
                icon = "💧",
                accentColor = Color(0xFF3B82F6),
                progress = safeProgress(uiState.waterMl, uiState.waterGoalMl ?: 0),
                modifier = Modifier.weight(1f),
                onClick = onWaterClick
            )

            TrackingCard(
                title = "Schritte",
                value = uiState.stepGoal?.let { goal -> "${uiState.steps} / $goal" } ?: "${uiState.steps} Schritte",
                icon = "👣",
                accentColor = Color(0xFFA855F7),
                progress = safeProgress(uiState.steps, uiState.stepGoal ?: 0),
                modifier = Modifier.weight(1f),
                onClick = onStepsClick
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(92.dp)
                    .background(Color(0xFF1F2937), RoundedCornerShape(18.dp))
                    .clickable { onWeightClick() }
                    .padding(14.dp)
            ) {
                Text(
                    text = "Gewicht",
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.TopStart)
                )

                Column(
                    modifier = Modifier.align(Alignment.BottomStart),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "${uiState.currentWeight ?: "--"} kg",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ziel: ${uiState.targetWeight ?: "--"} kg",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
                Text(
                    text = ">",
                    color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(92.dp)
                    .background(Color(0xFF1F2937), RoundedCornerShape(18.dp))
                    .clickable { onFastingClick() }
                    .padding(14.dp)
            ) {
                Text(
                    text = "Fasten",
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.TopStart)
                )

                Column(
                    modifier = Modifier.align(Alignment.BottomStart),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (uiState.fastingProgress != null) {
                        Text(
                            text = "${uiState.fastingTypeText} Methode",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (uiState.isFasting) "Aktiv" else "Geplant",
                            color = if (uiState.isFasting) Color(0xFF22C55E) else Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    } else {
                        Text(
                            text = "Einrichten",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Klicke zum Starten",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 10.sp
                        )
                    }
                }
                Text(
                    text = ">",
                    color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }

        Spacer(Modifier.weight(1f))
    }
}

private fun safeProgress(value: Int, goal: Int): Float {
    if (goal <= 0 || value <= 0) return 0f
    return (value.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
}

private fun formatLiter(ml: Int): String {
    return String.format("%.1f", ml / 1000.0)
}
