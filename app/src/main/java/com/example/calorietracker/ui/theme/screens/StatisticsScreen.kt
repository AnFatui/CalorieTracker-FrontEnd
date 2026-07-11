package com.example.calorietracker.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calorietracker.ui.viewmodel.DailyChartValue
import com.example.calorietracker.ui.viewmodel.StatisticsViewModel

@Composable
fun StatisticsScreen(
    onWaterClick: () -> Unit,
    viewModel: StatisticsViewModel,
    onSetLoading: (Boolean) -> Unit
) {
    var selectedTab by remember { mutableStateOf("Woche") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Tab("Woche", selectedTab == "Woche") { selectedTab = "Woche" }
            Tab("Monat", selectedTab == "Monat") { selectedTab = "Monat" }
            Tab("Jahr", selectedTab == "Jahr") { selectedTab = "Jahr" }
        }

        Spacer(Modifier.height(22.dp))

        when (selectedTab) {
            "Woche" -> {
                StatisticCard("Kalorien diese Woche", height = 220.dp) {
                    if (uiState.weeklyCalories.all { it.value == 0 }) {
                        EmptyInfo("Noch keine Kalorien eingetragen.")
                    } else {
                        WeeklyBarChart(uiState.weeklyCalories, Color(0xFF22C55E))
                    }
                }

                Spacer(Modifier.height(14.dp))

                StatisticCard("Wasser diese Woche", height = 220.dp, onClick = onWaterClick) {
                    if (uiState.weeklyWater.all { it.value == 0 }) {
                        EmptyInfo("Noch kein Wasser eingetragen.")
                    } else {
                        WeeklyBarChart(uiState.weeklyWater, Color(0xFF3B82F6))
                    }
                }

                Spacer(Modifier.height(14.dp))

                StatisticCard("Makronährstoffe diese Woche", height = 190.dp) {
                    MacroProgressLine(
                        "Protein",
                        uiState.proteins ?: 0,
                        uiState.proteinGoal ?: 0,
                        Color(0xFFEF4444)
                    )
                    MacroProgressLine(
                        "Kohlenhydrate",
                        uiState.carbs ?: 0,
                        uiState.carbGoal ?: 0,
                        Color(0xFFF97316)
                    )
                    MacroProgressLine(
                        "Fette",
                        uiState.fat ?: 0,
                        uiState.fatGoal ?: 0,
                        Color(0xFFFACC15)
                    )
                }
            }

            "Monat" -> {
                StatisticCard("Monatsübersicht") {
                    if (true /* appState.meals.isEmpty() && appState.waterMl == 0 && appState.weightEntries.isEmpty()*/) {
                        EmptyInfo("Noch keine Monatsdaten vorhanden.")
                    } else {
                        Text(
                            //"Mahlzeiten: ${appState.meals.size}",
                            "",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            //"Kalorien gesamt: ${appState.calories} kcal",
                            "",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            //"Wasser heute: ${appState.waterMl} ml",
                            "",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                StatisticCard("Monatliche Entwicklung") {
                    EmptyInfo("Detaillierte Monatsstatistiken werden später mit der Datenbank berechnet.")
                }
            }

            "Jahr" -> {
                StatisticCard("Jahresübersicht") {
                    EmptyInfo("Noch keine Jahresdaten vorhanden.")
                }

                Spacer(Modifier.height(14.dp))

                StatisticCard("Langzeitentwicklung") {
                    EmptyInfo("Jahresvergleiche werden später aus gespeicherten Daten erstellt.")
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun StatisticCard(
    title: String,
    height: Dp = 155.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = height)
            .background(Color(0xFF1F2937), RoundedCornerShape(18.dp))
            .let { if (onClick != null) it.clickable { onClick() } else it }
            .padding(14.dp)
    ) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        content()
    }
}

@Composable
private fun WeeklyBarChart(data: List<DailyChartValue>, barColor: Color) {
    val maxValue = (data.maxOfOrNull { it.value } ?: 0).coerceAtLeast(1)
    val maxBarHeight = 100.dp

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxBarHeight + 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { day ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (day.value > 0) "${day.value}" else "",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 9.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    val barHeight = if (day.value > 0) {
                        (maxBarHeight * (day.value.toFloat() / maxValue.toFloat())).coerceAtLeast(4.dp)
                    } else {
                        2.dp
                    }
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(barHeight)
                            .background(
                                if (day.isToday) barColor else barColor.copy(alpha = 0.45f),
                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEach { day ->
                Text(
                    text = day.dayLabel,
                    color = if (day.isToday) barColor else Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun EmptyInfo(text: String) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(text, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
private fun Tab(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (selected) Color(0xFF22C55E) else Color(0xFF1F2937),
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 28.dp, vertical = 12.dp)
    ) {
        Text(
            text,
            color = if (selected) Color.Black else Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MacroProgressLine(label: String, value: Int, goal: Int, color: Color) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White, fontSize = 12.sp)
            Text("$value / $goal g", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
        }

        Spacer(Modifier.height(5.dp))

        ProgressBar(value, goal, color)
    }
}

@Composable
private fun ProgressBar(value: Int, goal: Int, color: Color) {
    val progress = if (goal <= 0 || value <= 0) 0f else value.toFloat() / goal.toFloat()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(Color(0xFF374151), RoundedCornerShape(99.dp))
    ) {
        if (progress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .widthIn(min = 8.dp)
                    .height(8.dp)
                    .background(color, RoundedCornerShape(99.dp))
            )
        }
    }
}