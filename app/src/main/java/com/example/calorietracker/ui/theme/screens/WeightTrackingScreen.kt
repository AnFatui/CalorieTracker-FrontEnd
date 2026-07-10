package com.example.calorietracker.ui.theme.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calorietracker.data.model.WeightLog
import com.example.calorietracker.ui.viewmodel.WeightTimeRange
import com.example.calorietracker.ui.viewmodel.WeightTrackingViewModel
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun WeightTrackingScreen(
    viewModel: WeightTrackingViewModel,
    onSetLoading: (Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.loading) {
        onSetLoading(uiState.loading)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                WeightAdjustButton("-") { viewModel.adjustWeight(-0.1) }

                Spacer(Modifier.width(24.dp))

                val weightDisplay =
                    uiState.currentWeightKg?.let { "%.1f".format(it) } ?: "--.-"
                Text(
                    text = "$weightDisplay kg",
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.width(24.dp))

                WeightAdjustButton("+") { viewModel.adjustWeight(0.1) }
            }

            Text(
                text = "Zielgewicht: ${uiState.targetWeightKg ?: "-"} kg",
                color = Color(0xFF22C55E),
                fontSize = 14.sp
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    // 1. Die Grafik-Box nach oben verschoben
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .background(Color(0xFF111827), RoundedCornerShape(24.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.weightLogs.isEmpty()) {
                            Text(
                                "Noch kein Verlauf vorhanden.",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        } else {
                            WeightGraph(
                                logs = uiState.weightLogs,
                                timeRange = uiState.selectedTimeRange
                            )
                        }
                    }

                    // Abstand zwischen Grafik und Buttons
                    Spacer(Modifier.height(12.dp))

                    // 2. Die Buttons nach unten verschoben und auf volle Breite gestreckt
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TimeRangeButton(
                            text = "3 Monate",
                            isSelected = uiState.selectedTimeRange == WeightTimeRange.WEEK,
                            modifier = Modifier.weight(1f) // Nimmt 50% des verfügbaren Platzes ein
                        ) {
                            viewModel.setTimeRange(WeightTimeRange.WEEK)
                        }

                        Spacer(Modifier.width(8.dp)) // Kleiner Abstand zwischen den Buttons

                        TimeRangeButton(
                            text = "Jahr",
                            isSelected = uiState.selectedTimeRange == WeightTimeRange.YEAR,
                            modifier = Modifier.weight(1f) // Nimmt die anderen 50% ein
                        ) {
                            viewModel.setTimeRange(WeightTimeRange.YEAR)
                        }
                    }
                }
            }

            item {
                Text(
                    "Verlauf",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (uiState.weightLogs.isEmpty()) {
                item {
                    Text("Keine Einträge gefunden.", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                val logs = uiState.weightLogs.sortedByDescending { it.loggedAt }
                itemsIndexed(logs) { index, log ->
                    val shape = when {
                        logs.size == 1 -> RoundedCornerShape(16.dp)
                        index == 0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        index == logs.size - 1 -> RoundedCornerShape(
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        )

                        else -> RectangleShape
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1F2937), shape)
                            .padding(horizontal = 14.dp)
                    ) {
                        val date =
                            log.loggedAt.toLocalDateTime(TimeZone.currentSystemDefault()).date
                        val formattedDate = "${date.day.toString().padStart(2, '0')}.${
                            date.month.number.toString().padStart(2, '0')
                        }.${date.year}"

                        WeightRow(
                            date = formattedDate,
                            weight = "${"%.1f".format(log.weightKg)} kg"
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun TimeRangeButton(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .background(
                if (isSelected) Color(0xFF22C55E) else Color(0xFF1F2937),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun WeightGraph(logs: List<WeightLog>, timeRange: WeightTimeRange) {
    val nowInstant = Clock.System.now()
    val totalDays = if (timeRange == WeightTimeRange.YEAR) 365 else 90
    val startDate = nowInstant.minus(totalDays, DateTimeUnit.DAY, TimeZone.currentSystemDefault())

    val filteredLogs = logs.filter { it.loggedAt >= startDate }.sortedBy { it.loggedAt }

    if (filteredLogs.isEmpty()) {
        Text("Keine Daten für diesen Zeitraum.", color = Color.Gray, fontSize = 12.sp)
        return
    }

    val weights = filteredLogs.map { it.weightKg }
    val minWeight = (weights.minOrNull() ?: 0.0) - 1.0
    val maxWeight = (weights.maxOrNull() ?: 100.0) + 1.0
    val weightRange = if (maxWeight == minWeight) 1.0 else maxWeight - minWeight

    val startWeight = filteredLogs.first().weightKg
    val endWeight = filteredLogs.last().weightKg

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Start: %.1f kg".format(startWeight),
                color = Color.Gray,
                fontSize = 10.sp
            )
            Text(
                text = "Aktuell: %.1f kg".format(endWeight),
                color = Color(0xFF22C55E),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.weight(1f)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Draw vertical grid lines (12 segments)
                val segments = 12
                for (i in 0..segments) {
                    val x = (i.toFloat() / segments) * width
                    drawLine(
                        color = Color.White.copy(alpha = 0.1f),
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val startMillis = startDate.toEpochMilliseconds()
                val endMillis = nowInstant.toEpochMilliseconds()
                val totalTime = endMillis - startMillis

                if (filteredLogs.size > 1) {
                    val path = Path()
                    filteredLogs.forEachIndexed { index, log ->
                        val logTime = log.loggedAt.toEpochMilliseconds()
                        val x = ((logTime - startMillis).toFloat() / totalTime) * width
                        val y =
                            height - (((log.weightKg - minWeight) / weightRange).toFloat() * height)

                        if (index == 0) path.moveTo(x, y)
                        else path.lineTo(x, y)

                        drawCircle(
                            color = Color(0xFF22C55E),
                            radius = 3.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }

                    drawPath(
                        path = path,
                        color = Color(0xFF22C55E),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                } else if (filteredLogs.size == 1) {
                    val logTime = filteredLogs[0].loggedAt.toEpochMilliseconds()
                    val x = ((logTime - startMillis).toFloat() / totalTime) * width
                    val y =
                        height - (((filteredLogs[0].weightKg - minWeight) / weightRange).toFloat() * height)
                    drawCircle(
                        color = Color(0xFF22C55E),
                        radius = 6.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Datumsbeschriftung (Links & Rechts)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val firstDate = startDate.toLocalDateTime(TimeZone.currentSystemDefault()).date
            val lastDate = nowInstant.toLocalDateTime(TimeZone.currentSystemDefault()).date

            Text(
                text = "${
                    firstDate.day.toString().padStart(2, '0')
                }.${firstDate.month.number.toString().padStart(2, '0')}.",
                color = Color.Gray,
                fontSize = 10.sp
            )

            Text(
                text = "${
                    lastDate.day.toString().padStart(2, '0')
                }.${lastDate.month.number.toString().padStart(2, '0')}.",
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun WeightAdjustButton(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(Color(0xFF1F2937), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun WeightRow(date: String, weight: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(date, color = Color.White, fontSize = 14.sp)
        Text(weight, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
