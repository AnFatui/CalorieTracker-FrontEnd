package com.example.calorietracker.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calorietracker.ui.theme.components.AppBottomBar
import com.example.calorietracker.ui.viewmodel.StatisticsViewModel

@Composable
fun StatisticsScreen(
    onHomeClick: () -> Unit,
    onMealsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onWaterClick: () -> Unit,
    viewModel: StatisticsViewModel
) {
    var selectedTab by remember { mutableStateOf("Woche") }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(42.dp))

        Text("Statistiken", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(18.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Tab("Woche", selectedTab == "Woche") { selectedTab = "Woche" }
            Tab("Monat", selectedTab == "Monat") { selectedTab = "Monat" }
            Tab("Jahr", selectedTab == "Jahr") { selectedTab = "Jahr" }
        }

        Spacer(Modifier.height(22.dp))

        when (selectedTab) {
            "Woche" -> {
                StatisticCard("Kalorien diese Woche") {
                    val calories = uiState.calories
                    val calorieGoal = uiState.calorieGoal
                    if (calories == null) EmptyInfo("Noch keine Kalorien eingetragen.")
                    else {
                        Text(
                            "${uiState.calories} / ${uiState.calorieGoal} kcal",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        if (calorieGoal != null) ProgressBar(
                            calories,
                            calorieGoal,
                            Color(0xFF22C55E)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                StatisticCard("Makronährstoffe diese Woche") {
                    val proteins = uiState.proteins
                    val proteinGoal = uiState.proteinGoal

                    val fat = uiState.fat
                    val fatGoal = uiState.fatGoal

                    val carbs = uiState.carbs
                    val carbGoal = uiState.carbGoal

                    if (proteins != null && proteinGoal != null) MacroProgressLine(
                        "Protein",
                        proteins,
                        proteinGoal,
                        Color(0xFFEF4444)
                    )
                    if (carbs != null && carbGoal != null) MacroProgressLine(
                        "Kohlenhydrate",
                        carbs,
                        carbGoal,
                        Color(0xFFF97316)
                    )
                    if (fat != null && fatGoal != null) MacroProgressLine(
                        "Fette",
                        fat,
                        fatGoal,
                        Color(0xFFFACC15)
                    )
                }
            }

            "Monat" -> {
                StatisticCard("Monatsübersicht") {
                    if (appState.meals.isEmpty() && appState.waterMl == 0 && appState.weightEntries.isEmpty()) {
                        EmptyInfo("Noch keine Monatsdaten vorhanden.")
                    } else {
                        Text(
                            "Mahlzeiten: ${appState.meals.size}",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Kalorien gesamt: ${appState.calories} kcal",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Wasser heute: ${appState.waterMl} ml",
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

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1F2937), RoundedCornerShape(16.dp))
                .clickable { onWaterClick() }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "💧 Wasser   ${uiState.waterMl} / ${uiState.waterMlGoal} ml   >",
                color = Color.White
            )
        }

        Spacer(Modifier.weight(1f))

        AppBottomBar(
            selected = "Statistics",
            onHomeClick = onHomeClick,
            onMealsClick = onMealsClick,
            onAddClick = onMealsClick,
            onStatisticsClick = {},
            onProfileClick = onProfileClick
        )
    }
}

@Composable
private fun StatisticCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(155.dp)
            .background(Color(0xFF1F2937), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        content()
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
                    .height(8.dp)
                    .background(color, RoundedCornerShape(99.dp))
            )
        }
    }
}