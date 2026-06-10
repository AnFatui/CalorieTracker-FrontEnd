package com.example.calorietracker.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RecipesScreen(onRecipeClick: () -> Unit) {
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Alle") }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(42.dp))

        Text("Rezepte", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Rezepte suchen...", color = Color.Gray, fontSize = 12.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(18.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1F2937),
                unfocusedContainerColor = Color(0xFF1F2937),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.White
            ),
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)
        )

        Spacer(Modifier.height(22.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("Alle", selectedCategory == "Alle") { selectedCategory = "Alle" }
            Chip("Frühstück", selectedCategory == "Frühstück") { selectedCategory = "Frühstück" }
            Chip("Mittagessen", selectedCategory == "Mittagessen") { selectedCategory = "Mittagessen" }
            Chip("Abendessen", selectedCategory == "Abendessen") { selectedCategory = "Abendessen" }
        }

        Spacer(Modifier.height(24.dp))

        Text("Beliebt", color = Color.White, fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            RecipeSmall("Protein Pancake", Modifier.weight(1f), onRecipeClick)
            RecipeSmall("Hähnchen Bowl", Modifier.weight(1f), onRecipeClick)
        }

        Spacer(Modifier.height(24.dp))

        Text("Neueste Rezepte", color = Color.White, fontWeight = FontWeight.Bold)

        RecipeRow("Schoko Protein Smoothie", "350 kcal - 10 min", onRecipeClick)
        RecipeRow("Avocado Toast", "400 kcal - 15 min", onRecipeClick)
    }
}

@Composable
private fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .background(
                if (selected) Color(0xFF22C55E) else Color(0xFF1F2937),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(text, color = Color.White, fontSize = 11.sp)
    }
}

@Composable
private fun RecipeSmall(title: String, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .background(Color(0xFF1F2937), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color(0xFF4B5563), RoundedCornerShape(12.dp)),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("Bild", color = Color.White)
        }

        Text(title, color = Color.White, fontSize = 11.sp)
    }
}

@Composable
private fun RecipeRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .background(Color(0xFF1F2937), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Box(
            Modifier
                .size(70.dp)
                .background(Color(0xFF4B5563), RoundedCornerShape(12.dp)),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("Bild", color = Color.White)
        }

        Spacer(Modifier.width(16.dp))

        Column {
            Text(title, color = Color.White, fontSize = 12.sp)
            Text(subtitle, color = Color.White, fontSize = 11.sp)
        }
    }
}