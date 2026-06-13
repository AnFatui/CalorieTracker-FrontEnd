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
import com.example.calorietracker.ui.theme.components.PrimaryButton

@Composable
fun RecipeDetailScreen(onBackClick: () -> Unit) {
    var isFavorite by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(20.dp)
    ) {
        Spacer(Modifier.height(36.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "←",
                color = Color.White,
                fontSize = 30.sp,
                modifier = Modifier.clickable { onBackClick() }
            )

            Text(
                if (isFavorite) "♥" else "♡",
                color = if (isFavorite) Color(0xFFEF4444) else Color.White,
                fontSize = 28.sp,
                modifier = Modifier.clickable { isFavorite = !isFavorite }
            )
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color(0xFF1F2937), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("🥞", fontSize = 52.sp)
        }

        Spacer(Modifier.height(22.dp))

        Text("Protein Pancakes", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("⭐ 4,8 (128)   •   35 min", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)

        Spacer(Modifier.height(22.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1F2937), RoundedCornerShape(16.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Nutrient("Kalorien", "520")
            Nutrient("Protein", "35 g")
            Nutrient("Carbs", "60 g")
            Nutrient("Fette", "15 g")
        }

        Spacer(Modifier.height(26.dp))

        Text("Zutaten", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Ingredient("Haferflocken", "50 g")
        Ingredient("Ei", "1 Stk.")
        Ingredient("Protein Pulver", "30 g")
        Ingredient("Milch", "100 ml")
        Ingredient("Beeren", "50 g")

        Spacer(Modifier.weight(1f))

        PrimaryButton("Zu Mahlzeit hinzufügen", onBackClick)

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun Nutrient(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(alpha = 0.65f), fontSize = 9.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun Ingredient(name: String, amount: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("●  $name", color = Color.White, fontSize = 13.sp)
        Text(amount, color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
    }
}