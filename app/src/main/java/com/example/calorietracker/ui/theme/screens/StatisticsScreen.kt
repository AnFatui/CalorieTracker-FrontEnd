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
import com.example.calorietracker.ui.theme.AppUiState
import com.example.calorietracker.ui.theme.components.AppBottomBar

@Composable
fun StatisticsScreen(
    appState: AppUiState,
    onHomeClick: () -> Unit,
    onMealsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onWaterClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("Woche") }
    val hasNutritionData = appState.calories > 0 || appState.protein > 0 || appState.carbs > 0 || appState.fat > 0

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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
                .background(Color(0xFF1F2937), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Text("Kalorien", color = Color.White, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(12.dp))

            if (appState.calories == 0) {
                EmptyInfo("Noch keine Kalorien eingetragen.")
            } else {
                Text("${appState.calories} / ${appState.calorieGoal} kcal", color = Color.White, fontSize = 12.sp)
                Spacer(Modifier.height(12.dp))
                ProgressBar(appState.calories, appState.calorieGoal, Color(0xFF22C55E))
            }
        }

        Spacer(Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color(0xFF1F2937), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Text("Makronährstoffe", color = Color.White, fontWeight = FontWeight.Bold)

            if (!hasNutritionData) {
                Spacer(Modifier.height(18.dp))
                EmptyInfo("Noch keine Makronährstoffe eingetragen.")
            } else {
                MacroProgressLine("Protein", appState.protein, appState.proteinGoal, Color(0xFFEF4444))
                MacroProgressLine("Kohlenhydrate", appState.carbs, appState.carbsGoal, Color(0xFFF97316))
                MacroProgressLine("Fette", appState.fat, appState.fatGoal, Color(0xFFFACC15))
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1F2937), RoundedCornerShape(14.dp))
                .clickable { onWaterClick() }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "💧 Wasser   ${appState.waterMl} / ${appState.waterGoalMl} ml   >",
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
private fun EmptyInfo(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
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
        Text(text, color = Color.White, fontSize = 11.sp)
    }
}

@Composable
private fun MacroProgressLine(label: String, value: Int, goal: Int, color: Color) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White, fontSize = 12.sp)
            Text("$value / $goal g", color = Color.White, fontSize = 12.sp)
        }

        Spacer(Modifier.height(6.dp))

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