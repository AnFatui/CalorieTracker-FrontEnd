package com.example.calorietracker.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calorietracker.data.model.Recipe
import com.example.calorietracker.ui.theme.components.PrimaryButton
import com.example.calorietracker.ui.viewmodel.RecipeDetailViewModel

@Composable
fun RecipeDetailScreen(
    recipeId: String,
    onBackClick: () -> Unit,
    onAddToMeal: (Recipe) -> Unit,
    onSetLoading: (Boolean) -> Unit,
    viewModel: RecipeDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(recipeId) {
        viewModel.loadRecipe(recipeId)
    }

    LaunchedEffect(uiState.loading) {
        onSetLoading(uiState.loading)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(20.dp)
    ) {
        Text(
            "←",
            color = Color.White,
            fontSize = 30.sp,
            modifier = Modifier.clickable { onBackClick() }
        )

        Spacer(Modifier.height(12.dp))

        val recipe = uiState.recipe
        if (recipe == null) {
            if (!uiState.loading) {
                Text("Rezept nicht gefunden.", color = Color.White)
            }
            return@Column
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color(0xFF1F2937), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(recipe.emoji, fontSize = 52.sp)
        }

        Spacer(Modifier.height(22.dp))

        Text(recipe.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(recipe.category, color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)

        Spacer(Modifier.height(22.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1F2937), RoundedCornerShape(16.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Nutrient("Kalorien", "${recipe.calories}")
            Nutrient("Protein", "${recipe.protein} g")
            Nutrient("Carbs", "${recipe.carbs} g")
            Nutrient("Fette", "${recipe.fat} g")
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "Die Werte kannst du beim Hinzufügen noch anpassen.",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp
        )

        Spacer(Modifier.weight(1f))

        PrimaryButton(text = "Zu Mahlzeit hinzufügen", onClick = { onAddToMeal(recipe) })
    }
}

@Composable
private fun Nutrient(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(alpha = 0.65f), fontSize = 9.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}
