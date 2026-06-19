package com.example.calorietracker.ui.theme.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calorietracker.ui.theme.components.AppBottomBar
import com.example.calorietracker.ui.viewmodel.UserViewModel
import com.example.calorietracker.ui.viewmodel.WeightTrackingViewModel
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun WeightTrackingScreen(
    onBackClick: () -> Unit,
    viewModel: WeightTrackingViewModel
) {
    val dataState by viewModel.dataUiState.collectAsState()
    var newWeightEntry by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(42.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "←",
                color = Color.White,
                fontSize = 30.sp,
                modifier = Modifier.clickable { onBackClick() })

            Text("Gewicht", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)

            Text(
                "+",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    if (newWeightEntry.isNotBlank() && newWeightEntry.toDoubleOrNull() != null) {
                        val entry = newWeightEntry.toDouble()
                        viewModel.addWeightLog(entry)
                    }
                }
            )
        }

        Spacer(Modifier.height(28.dp))
        val weightText: String =
            if (dataState.currentWeightKg != null) "${dataState.currentWeightKg} KG"
            else "Kein Gewichtsdaten vorhanden"

        Text(
            text = weightText,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Text(
            text = "Zielgewicht: ${dataState.targetWeightKg} kg",
            color = Color(0xFF22C55E),
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = newWeightEntry,
            onValueChange = { input ->
                newWeightEntry = input.filter { it.isDigit() || it == ',' || it == '.' }
            },
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
            if (dataState.weightLogs.isEmpty()) {
                Text("Noch kein Gewichtsverlauf vorhanden.", color = Color.Gray, fontSize = 12.sp)
            } else {
                Canvas(
                    Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
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
            if (dataState.weightLogs.isEmpty()) {
                WeightRow("Aktueller Eintrag", "-")
                WeightRow(
                    "Zielgewicht",
                    if (dataState.targetWeightKg == null) "-" else "${dataState.targetWeightKg} kg"
                )
            } else {
                dataState.weightLogs.reversed().forEach {
                    WeightRow(it.loggedAt.toString(), "${it.weightKg} kg")
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