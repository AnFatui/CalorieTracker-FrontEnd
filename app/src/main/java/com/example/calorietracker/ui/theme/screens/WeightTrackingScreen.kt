package com.example.calorietracker.ui.theme.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calorietracker.ui.theme.AppUiState
import com.example.calorietracker.ui.theme.WeightEntry
import com.example.calorietracker.ui.theme.components.AppBottomBar

@Composable
fun WeightTrackingScreen(
    appState: AppUiState,
    onStateChange: (AppUiState) -> Unit,
    onBackClick: () -> Unit
) {
    var newWeight by remember { mutableStateOf("") }
    val hasWeight = appState.currentWeight.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(42.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("←", color = Color.White, fontSize = 30.sp, modifier = Modifier.clickable { onBackClick() })

            Text("Gewicht", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)

            Text(
                "+",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    if (newWeight.isNotBlank()) {
                        val newEntry = WeightEntry(newWeight, "Heute")
                        onStateChange(
                            appState.copy(
                                currentWeight = newWeight,
                                weightEntries = appState.weightEntries + newEntry
                            )
                        )
                        newWeight = ""
                    }
                }
            )
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text = if (hasWeight) "${appState.currentWeight} kg" else "Noch kein Gewicht",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Text(
            text = if (appState.targetWeight.isBlank()) "Kein Zielgewicht gesetzt" else "Zielgewicht: ${appState.targetWeight} kg",
            color = Color(0xFF22C55E),
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = newWeight,
            onValueChange = { input -> newWeight = input.filter { it.isDigit() || it == ',' || it == '.' } },
            placeholder = { Text("Neues Gewicht eintragen", color = Color.Gray) },
            suffix = { Text("kg", color = Color.White) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
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

        Spacer(Modifier.height(26.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .background(Color(0xFF050505), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (appState.weightEntries.isEmpty()) {
                Text("Noch kein Gewichtsverlauf vorhanden.", color = Color.Gray, fontSize = 12.sp)
            } else {
                Canvas(Modifier.fillMaxSize().padding(18.dp)) {
                    val points = listOf(
                        Offset(10f, size.height * 0.65f),
                        Offset(size.width * 0.35f, size.height * 0.55f),
                        Offset(size.width * 0.7f, size.height * 0.48f),
                        Offset(size.width - 10f, size.height * 0.42f)
                    )

                    for (i in 0 until points.size - 1) {
                        drawLine(Color(0xFF22C55E), points[i], points[i + 1], strokeWidth = 5f)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text("Verlauf", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Column(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFF1F2937), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            if (appState.weightEntries.isEmpty()) {
                WeightRow("Aktueller Eintrag", "-")
                WeightRow("Zielgewicht", if (appState.targetWeight.isBlank()) "-" else "${appState.targetWeight} kg")
            } else {
                appState.weightEntries.reversed().forEach {
                    WeightRow(it.dateLabel, "${it.value} kg")
                }
            }
        }

        Spacer(Modifier.weight(1f))

        AppBottomBar(
            selected = "Statistics",
            onHomeClick = onBackClick,
            onMealsClick = {},
            onAddClick = {},
            onStatisticsClick = {},
            onProfileClick = {}
        )
    }
}

@Composable
private fun WeightRow(date: String, weight: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(date, color = Color.White, fontSize = 12.sp)
        Text(weight, color = Color.White, fontSize = 12.sp)
    }
}