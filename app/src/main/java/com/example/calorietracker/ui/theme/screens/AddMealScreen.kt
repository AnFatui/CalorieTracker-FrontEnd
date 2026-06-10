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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calorietracker.ui.theme.AppUiState
import com.example.calorietracker.ui.theme.components.PrimaryButton

@Composable
fun AddMealScreen(
    appState: AppUiState,
    onStateChange: (AppUiState) -> Unit,
    onBackClick: () -> Unit
) {
    var selectedMode by remember { mutableStateOf("Manuell") }
    var mealName by remember { mutableStateOf("") }
    var caloriesInput by remember { mutableStateOf("") }
    var proteinInput by remember { mutableStateOf("") }
    var carbsInput by remember { mutableStateOf("") }
    var fatInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(42.dp))

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "←",
                color = Color.White,
                fontSize = 30.sp,
                modifier = Modifier.clickable { onBackClick() }
            )

            Spacer(Modifier.width(18.dp))

            Text(
                "Mahlzeit hinzufügen",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ModeChip("Rezept wählen", selectedMode == "Rezept wählen") {
                selectedMode = "Rezept wählen"
            }

            ModeChip("KI Erkennung", selectedMode == "KI Erkennung") {
                selectedMode = "KI Erkennung"
            }

            ModeChip("Manuell", selectedMode == "Manuell") {
                selectedMode = "Manuell"
            }
        }

        Spacer(Modifier.height(26.dp))

        Text(
            text = "Trage die Nährwerte deiner Mahlzeit ein. Nach dem Speichern werden Dashboard und Statistik automatisch aktualisiert.",
            color = Color.White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        MealInput("Name der Mahlzeit", mealName, false) { mealName = it }
        Spacer(Modifier.height(12.dp))
        MealInput("Kalorien", caloriesInput, true) { caloriesInput = it.filter { c -> c.isDigit() } }
        Spacer(Modifier.height(12.dp))
        MealInput("Protein in g", proteinInput, true) { proteinInput = it.filter { c -> c.isDigit() } }
        Spacer(Modifier.height(12.dp))
        MealInput("Kohlenhydrate in g", carbsInput, true) { carbsInput = it.filter { c -> c.isDigit() } }
        Spacer(Modifier.height(12.dp))
        MealInput("Fette in g", fatInput, true) { fatInput = it.filter { c -> c.isDigit() } }

        Spacer(Modifier.weight(1f))

        PrimaryButton(
            text = "Mahlzeit speichern",
            onClick = {
                onStateChange(
                    appState.copy(
                        calories = appState.calories + (caloriesInput.toIntOrNull() ?: 0),
                        protein = appState.protein + (proteinInput.toIntOrNull() ?: 0),
                        carbs = appState.carbs + (carbsInput.toIntOrNull() ?: 0),
                        fat = appState.fat + (fatInput.toIntOrNull() ?: 0)
                    )
                )
                onBackClick()
            }
        )

        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun ModeChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                color = if (selected) Color(0xFF22C55E) else Color(0xFF1F2937),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(text, color = Color.White, fontSize = 10.sp)
    }
}

@Composable
private fun MealInput(
    placeholder: String,
    value: String,
    number: Boolean,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.Gray, fontSize = 13.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (number) KeyboardType.Number else KeyboardType.Text
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
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
}