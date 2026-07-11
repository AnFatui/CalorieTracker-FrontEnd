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
import com.example.calorietracker.ui.viewmodel.ChartValue
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
                        PeriodBarChart(uiState.weeklyCalories, Color(0xFF22C55E))
                    }
                }

                Spacer(Modifier.height(14.dp))

                StatisticCard("Wasser diese Woche", height = 220.dp, onClick = onWaterClick) {
                    if (uiState.weeklyWater.all { it.value == 0 }) {
                        EmptyInfo("Noch kein Wasser eingetragen.")
                    } else {
                        PeriodBarChart(uiState.weeklyWater, Color(0xFF3B82F6))
                    }
                }

                Spacer(Modifier.height(14.dp))

                StatisticCard("Schritte diese Woche", height = 220.dp) {
                    if (!uiState.hasStepsPermission) {
                        EmptyInfo("Für Schritte-Statistiken bitte Health Connect Berechtigung erteilen.")
                    } else if (uiState.weeklySteps.all { it.value == 0 }) {
                        EmptyInfo("Noch keine Schritte erfasst.")
                    } else {
                        PeriodBarChart(uiState.weeklySteps, Color(0xFFA855F7))
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
                StatisticCard("Kalorien diesen Monat", height = 220.dp) {
                    if (uiState.monthlyCalories.all { it.value == 0 }) {
                        EmptyInfo("Noch keine Kalorien eingetragen.")
                    } else {
                        PeriodBarChart(uiState.monthlyCalories, Color(0xFF22C55E))
                    }
                }

                Spacer(Modifier.height(14.dp))

                StatisticCard("Wasser diesen Monat", height = 220.dp, onClick = onWaterClick) {
                    if (uiState.monthlyWater.all { it.value == 0 }) {
                        EmptyInfo("Noch kein Wasser eingetragen.")
                    } else {
                        PeriodBarChart(uiState.monthlyWater, Color(0xFF3B82F6))
                    }
                }

                Spacer(Modifier.height(14.dp))

                StatisticCard("Schritte diesen Monat", height = 220.dp) {
                    if (!uiState.hasStepsPermission) {
                        EmptyInfo("Für Schritte-Statistiken bitte Health Connect Berechtigung erteilen.")
                    } else if (uiState.monthlySteps.all { it.value == 0 }) {
                        EmptyInfo("Noch keine Schritte erfasst.")
                    } else {
                        PeriodBarChart(uiState.monthlySteps, Color(0xFFA855F7))
                    }
                }

                Spacer(Modifier.height(14.dp))

                StatisticCard("Makronährstoffe diesen Monat", height = 190.dp) {
                    MacroProgressLine(
                        "Protein",
                        uiState.monthProteins ?: 0,
                        uiState.monthProteinGoal ?: 0,
                        Color(0xFFEF4444)
                    )
                    MacroProgressLine(
                        "Kohlenhydrate",
                        uiState.monthCarbs ?: 0,
                        uiState.monthCarbGoal ?: 0,
                        Color(0xFFF97316)
                    )
                    MacroProgressLine(
                        "Fette",
                        uiState.monthFat ?: 0,
                        uiState.monthFatGoal ?: 0,
                        Color(0xFFFACC15)
                    )
                }
            }

            "Jahr" -> {
                StatisticCard("Kalorien dieses Jahr", height = 220.dp) {
                    if (uiState.yearlyCalories.all { it.value == 0 }) {
                        EmptyInfo("Noch keine Kalorien eingetragen.")
                    } else {
                        PeriodBarChart(uiState.yearlyCalories, Color(0xFF22C55E))
                    }
                }

                Spacer(Modifier.height(14.dp))

                StatisticCard("Wasser dieses Jahr", height = 220.dp, onClick = onWaterClick) {
                    if (uiState.yearlyWater.all { it.value == 0 }) {
                        EmptyInfo("Noch kein Wasser eingetragen.")
                    } else {
                        PeriodBarChart(uiState.yearlyWater, Color(0xFF3B82F6))
                    }
                }

                Spacer(Modifier.height(14.dp))

                StatisticCard("Schritte dieses Jahr", height = 220.dp) {
                    if (!uiState.hasStepsPermission) {
                        EmptyInfo("Für Schritte-Statistiken bitte Health Connect Berechtigung erteilen.")
                    } else if (uiState.yearlySteps.all { it.value == 0 }) {
                        EmptyInfo("Noch keine Schritte erfasst.")
                    } else {
                        PeriodBarChart(uiState.yearlySteps, Color(0xFFA855F7))
                    }
                }

                Spacer(Modifier.height(14.dp))

                StatisticCard("Makronährstoffe dieses Jahr", height = 190.dp) {
                    MacroProgressLine(
                        "Protein",
                        uiState.yearProteins ?: 0,
                        uiState.yearProteinGoal ?: 0,
                        Color(0xFFEF4444)
                    )
                    MacroProgressLine(
                        "Kohlenhydrate",
                        uiState.yearCarbs ?: 0,
                        uiState.yearCarbGoal ?: 0,
                        Color(0xFFF97316)
                    )
                    MacroProgressLine(
                        "Fette",
                        uiState.yearFat ?: 0,
                        uiState.yearFatGoal ?: 0,
                        Color(0xFFFACC15)
                    )
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
private fun PeriodBarChart(data: List<ChartValue>, barColor: Color) {
    val maxValue = (data.maxOfOrNull { it.value } ?: 0).coerceAtLeast(1)
    val maxBarHeight = 100.dp
    val barWidth = if (data.size > 7) 10.dp else 18.dp
    val labelFontSize = if (data.size > 7) 9.sp else 11.sp

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxBarHeight + 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { entry ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (entry.value > 0) "${entry.value}" else "",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 9.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    val barHeight = if (entry.value > 0) {
                        (maxBarHeight * (entry.value.toFloat() / maxValue.toFloat())).coerceAtLeast(4.dp)
                    } else {
                        2.dp
                    }
                    Box(
                        modifier = Modifier
                            .width(barWidth)
                            .height(barHeight)
                            .background(
                                if (entry.isCurrent) barColor else barColor.copy(alpha = 0.45f),
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
            data.forEach { entry ->
                Text(
                    text = entry.label,
                    color = if (entry.isCurrent) barColor else Color.White.copy(alpha = 0.6f),
                    fontSize = labelFontSize,
                    fontWeight = if (entry.isCurrent) FontWeight.Bold else FontWeight.Normal
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