package com.example.calorietracker.ui.theme.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calorietracker.data.model.MealLog
import com.example.calorietracker.ui.viewmodel.MealTrackingViewModel

@Composable
fun MealsScreen(
    viewModel: MealTrackingViewModel,
    onAddMealClick: (String) -> Unit,
    onRecipeClick: () -> Unit
) {
    var selectedDay by remember { mutableStateOf("Mo") }
    val uiDataState by viewModel.uiDataState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(42.dp))

        Text("Mahlzeiten", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            DateBox("Mo\n20", selectedDay == "Mo") { selectedDay = "Mo" }
            DateBox("DI\n21", selectedDay == "Di") { selectedDay = "Di" }
            DateBox("MI\n22", selectedDay == "Mi") { selectedDay = "Mi" }
            DateBox("DO\n23", selectedDay == "Do") { selectedDay = "Do" }
            DateBox("FR\n24", selectedDay == "Fr") { selectedDay = "Fr" }
        }

        Spacer(Modifier.height(24.dp))

        MealSection(
            title = "Frühstück",
            meals = uiDataState.meals.filter { it.type == "Frühstück" },
            onAddClick = { onAddMealClick("Frühstück") },
            onRecipeClick = onRecipeClick
        )

        MealSection(
            title = "Mittagessen",
            meals = uiDataState.meals.filter { it.type == "Mittagessen" },
            onAddClick = { onAddMealClick("Mittagessen") },
            onRecipeClick = onRecipeClick
        )

        MealSection(
            title = "Abendessen",
            meals = uiDataState.meals.filter { it.type == "Abendessen" },
            onAddClick = { onAddMealClick("Abendessen") },
            onRecipeClick = onRecipeClick
        )

        MealSection(
            title = "Snacks",
            meals = uiDataState.meals.filter { it.type == "Snacks" },
            onAddClick = { onAddMealClick("Snacks") },
            onRecipeClick = onRecipeClick
        )

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun DateBox(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                if (selected) Color(0xFF22C55E) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
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
private fun MealSection(
    title: String,
    meals: List<MealLog>,
    onAddClick: () -> Unit,
    onRecipeClick: () -> Unit
) {
    val calories = meals.sumOf { it.calories }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .background(Color(0xFF1F2937), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold)
                Text("$calories kcal", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            }

            Text(
                "+",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onAddClick() }
            )
        }

        Spacer(Modifier.height(10.dp))

        if (meals.isEmpty()) {
            Text(
                text = "Noch keine Mahlzeit eingetragen",
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 12.sp
            )
        } else {
            meals.forEach { meal ->
                MealRow(meal = meal, onRecipeClick = onRecipeClick)
            }
        }
    }
}

@Composable
private fun MealRow(
    meal: MealLog,
    onRecipeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clickable { onRecipeClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFF374151), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("🍽", fontSize = 18.sp)
        }

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(meal.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(
                "${meal.protein}g Protein • ${meal.carbs}g Carbs • ${meal.fat}g Fett",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp
            )
        }

        Text("${meal.calories} kcal", color = Color.White, fontSize = 11.sp)
    }
}