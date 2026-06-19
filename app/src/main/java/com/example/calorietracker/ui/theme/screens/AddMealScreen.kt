package com.example.calorietracker.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.calorietracker.ui.theme.components.PrimaryButton
import com.example.calorietracker.ui.viewmodel.AddMealViewModel

@Composable
fun AddMealScreen(
    viewModel: AddMealViewModel,
    onBackClick: () -> Unit,
    onRecipesClick: () -> Unit
) {
    val selectedMealType = "Frühstück"
    var selectedMode by remember { mutableStateOf("Manuell") }
    var selectedMealTypeLocal by remember { mutableStateOf(selectedMealType) }

    var mealName by remember { mutableStateOf("") }
    var caloriesInput by remember { mutableStateOf("") }
    var proteinInput by remember { mutableStateOf("") }
    var carbsInput by remember { mutableStateOf("") }
    var fatInput by remember { mutableStateOf("") }

    val canSave = mealName.isNotBlank() &&
            caloriesInput.isNotBlank() &&
            proteinInput.isNotBlank() &&
            carbsInput.isNotBlank() &&
            fatInput.isNotBlank()

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

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ModeChip("Rezept wählen", selectedMode == "Rezept wählen") {
                selectedMode = "Rezept wählen"
                onRecipesClick()
            }

            ModeChip("KI Erkennung", selectedMode == "KI Erkennung") {
                selectedMode = "KI Erkennung"
            }

            ModeChip("Manuell", selectedMode == "Manuell") {
                selectedMode = "Manuell"
            }
        }

        Spacer(Modifier.height(20.dp))

        if (selectedMode == "KI Erkennung") {
            AiRecognitionContent()
        } else {
            ManualMealContent(
                selectedMealTypeLocal = selectedMealTypeLocal,
                onMealTypeChange = { selectedMealTypeLocal = it },
                mealName = mealName,
                onMealNameChange = { mealName = it },
                caloriesInput = caloriesInput,
                onCaloriesChange = { caloriesInput = it.filter { c -> c.isDigit() } },
                proteinInput = proteinInput,
                onProteinChange = { proteinInput = it.filter { c -> c.isDigit() } },
                carbsInput = carbsInput,
                onCarbsChange = { carbsInput = it.filter { c -> c.isDigit() } },
                fatInput = fatInput,
                onFatChange = { fatInput = it.filter { c -> c.isDigit() } }
            )

            Spacer(Modifier.weight(1f))

            PrimaryButton(
                text = "Mahlzeit speichern",
                enabled = canSave,
                onClick = {
                    val calories = caloriesInput.toIntOrNull() ?: 0
                    val protein = proteinInput.toIntOrNull() ?: 0
                    val carbs = carbsInput.toIntOrNull() ?: 0
                    val fat = fatInput.toIntOrNull() ?: 0

//                    val newMeal = MealEntry(
//                        name = mealName,
//                        mealType = selectedMealTypeLocal,
//                        calories = calories,
//                        protein = protein,
//                        carbs = carbs,
//                        fat = fat
//                    )
//
//                    onStateChange(
//                        appState.copy(
//                            calories = appState.calories + calories,
//                            protein = appState.protein + protein,
//                            carbs = appState.carbs + carbs,
//                            fat = appState.fat + fat,
//                            meals = appState.meals + newMeal
//                        )
//                    )

                    onBackClick()
                }
            )

            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun ManualMealContent(
    selectedMealTypeLocal: String,
    onMealTypeChange: (String) -> Unit,
    mealName: String,
    onMealNameChange: (String) -> Unit,
    caloriesInput: String,
    onCaloriesChange: (String) -> Unit,
    proteinInput: String,
    onProteinChange: (String) -> Unit,
    carbsInput: String,
    onCarbsChange: (String) -> Unit,
    fatInput: String,
    onFatChange: (String) -> Unit
) {
    Text(
        text = "Kategorie",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(10.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        MealTypeChip("Frühstück", selectedMealTypeLocal == "Frühstück", Modifier.weight(1f)) {
            onMealTypeChange("Frühstück")
        }

        MealTypeChip("Mittag", selectedMealTypeLocal == "Mittagessen", Modifier.weight(1f)) {
            onMealTypeChange("Mittagessen")
        }

        MealTypeChip("Abend", selectedMealTypeLocal == "Abendessen", Modifier.weight(1f)) {
            onMealTypeChange("Abendessen")
        }

        MealTypeChip("Snack", selectedMealTypeLocal == "Snacks", Modifier.weight(1f)) {
            onMealTypeChange("Snacks")
        }
    }

    Spacer(Modifier.height(20.dp))

    Text(
        text = "Trage die Nährwerte ein. Nach dem Speichern werden Dashboard, Mahlzeiten und Statistik aktualisiert.",
        color = Color.White.copy(alpha = 0.75f),
        fontSize = 12.sp,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(20.dp))

    MealInput("Name der Mahlzeit", mealName, false, onMealNameChange)
    Spacer(Modifier.height(10.dp))
    MealInput("Kalorien", caloriesInput, true, onCaloriesChange)
    Spacer(Modifier.height(10.dp))
    MealInput("Protein in g", proteinInput, true, onProteinChange)
    Spacer(Modifier.height(10.dp))
    MealInput("Kohlenhydrate in g", carbsInput, true, onCarbsChange)
    Spacer(Modifier.height(10.dp))
    MealInput("Fette in g", fatInput, true, onFatChange)
}

@Composable
private fun AiRecognitionContent() {
    Spacer(Modifier.height(22.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .border(1.dp, Color(0xFF374151), RoundedCornerShape(24.dp))
            .background(Color(0xFF111827), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .background(Color(0xFF1F2937), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("📷", fontSize = 34.sp)
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Foto einer Mahlzeit aufnehmen",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Die KI-Erkennung wird später mit der Datenbank bzw. Supabase verbunden.",
                color = Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 26.dp)
            )
        }
    }

    Spacer(Modifier.height(24.dp))

    PrimaryButton(
        text = "Foto hochladen",
        enabled = false,
        onClick = {}
    )

    Spacer(Modifier.height(12.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Color(0xFF1F2937), RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Kamera öffnen",
            color = Color.White.copy(alpha = 0.45f),
            fontWeight = FontWeight.Bold
        )
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
        Text(
            text = text,
            color = if (selected) Color.Black else Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MealTypeChip(
    text: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .background(
                color = if (selected) Color(0xFF22C55E) else Color(0xFF1F2937),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.Black else Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
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
            .height(56.dp),
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