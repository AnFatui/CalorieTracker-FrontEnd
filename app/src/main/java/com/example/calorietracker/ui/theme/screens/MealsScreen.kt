package com.example.calorietracker.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calorietracker.ui.theme.components.AppBottomBar

@Composable
fun MealsScreen(
    onHomeClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAddMealClick: () -> Unit,
    onRecipeClick: () -> Unit
) {
    var selectedDay by remember { mutableStateOf("Mo") }

    Column(
        Modifier
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

        Spacer(Modifier.height(28.dp))

        MealBox("Frühstück", "0 kcal", "Noch keine Mahlzeit eingetragen", onAddMealClick, onRecipeClick)
        MealBox("Mittagessen", "0 kcal", "Noch keine Mahlzeit eingetragen", onAddMealClick, onRecipeClick)
        MealBox("Abendessen", "0 kcal", "Noch keine Mahlzeit eingetragen", onAddMealClick, onRecipeClick)
        MealBox("Snacks", "0 kcal", "Noch keine Mahlzeit eingetragen", onAddMealClick, onRecipeClick)

        Spacer(Modifier.weight(1f))

        AppBottomBar("Meals", onHomeClick, {}, onAddMealClick, onStatisticsClick, onProfileClick)
    }
}

@Composable
private fun DateBox(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(48.dp)
            .background(
                if (selected) Color(0xFF22C55E) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 11.sp)
    }
}

@Composable
private fun MealBox(
    title: String,
    kcal: String,
    meal: String,
    onAdd: () -> Unit,
    onRecipe: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
            .background(Color(0xFF1F2937), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold)
                Text(kcal, color = Color.White, fontSize = 11.sp)
            }

            Text(
                "+",
                color = Color.White,
                fontSize = 22.sp,
                modifier = Modifier.clickable { onAdd() }
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            "Bild        $meal",
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.clickable { onRecipe() }
        )
    }
}