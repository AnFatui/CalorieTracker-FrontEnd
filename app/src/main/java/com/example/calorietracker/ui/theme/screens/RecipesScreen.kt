package com.example.calorietracker.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calorietracker.ui.viewmodel.RecipesViewModel

@Composable
fun RecipesScreen(
    onRecipeClick: () -> Unit,
    viewModel: RecipesViewModel
) {
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Alle") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 20.dp)
    ) {
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
            textStyle = TextStyle(color = Color.White)
        )

        Spacer(Modifier.height(22.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RecipeChip("Alle", selectedCategory == "Alle") { selectedCategory = "Alle" }
            RecipeChip("Frühstück", selectedCategory == "Frühstück") { selectedCategory = "Frühstück" }
            RecipeChip("Mittag", selectedCategory == "Mittag") { selectedCategory = "Mittag" }
            RecipeChip("Abend", selectedCategory == "Abend") { selectedCategory = "Abend" }
        }

        Spacer(Modifier.height(50.dp))

        EmptyRecipeState()
    }
}

@Composable
private fun EmptyRecipeState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1F2937), RoundedCornerShape(24.dp))
            .padding(horizontal = 24.dp, vertical = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(82.dp)
                .background(Color(0xFF374151), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("🍽", fontSize = 34.sp)
        }

        Spacer(Modifier.height(18.dp))

        Text(
            text = "Noch keine Rezepte vorhanden",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Rezepte werden später aus der Datenbank geladen. Sobald die Verbindung eingerichtet ist, erscheinen sie hier.",
            color = Color.Gray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RecipeChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (selected) Color(0xFF22C55E) else Color(0xFF1F2937),
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 13.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.Black else Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}