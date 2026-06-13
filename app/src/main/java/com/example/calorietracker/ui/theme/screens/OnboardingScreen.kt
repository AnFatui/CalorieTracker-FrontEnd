package com.example.calorietracker.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
fun OnboardingScreen(
    appState: AppUiState,
    onStateChange: (AppUiState) -> Unit,
    onContinueClick: () -> Unit
) {
    val isWeightMaintenance = appState.selectedGoal == "Gewicht halten"
    val canContinue = appState.selectedGoal.isNotBlank() &&
            appState.currentWeight.isNotBlank() &&
            (isWeightMaintenance || appState.targetWeight.isNotBlank())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(36.dp))

        Text("━  •  •  •", color = Color(0xFF22C55E), fontSize = 22.sp)

        Spacer(Modifier.height(28.dp))

        Text("Deine Ziele", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)

        Text(
            text = "Erzähl uns etwas über deine Ziele, damit wir dich bestmöglich unterstützen können.",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )

        Spacer(Modifier.height(32.dp))

        Text(
            "Mein Ziel ist es ...",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            GoalBox("Abnehmen", appState.selectedGoal == "Abnehmen", Modifier.weight(1f)) {
                onStateChange(appState.copy(selectedGoal = "Abnehmen"))
            }

            GoalBox("Muskeln\naufbauen", appState.selectedGoal == "Muskeln aufbauen", Modifier.weight(1f)) {
                onStateChange(appState.copy(selectedGoal = "Muskeln aufbauen"))
            }

            GoalBox("Gewicht\nhalten", appState.selectedGoal == "Gewicht halten", Modifier.weight(1f)) {
                onStateChange(appState.copy(selectedGoal = "Gewicht halten", targetWeight = ""))
            }
        }

        if (!isWeightMaintenance && appState.selectedGoal.isNotBlank()) {
            Spacer(Modifier.height(28.dp))

            Text(
                "Mein wöchentliches Ziel",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                "${String.format("%.1f", appState.weeklyGoal)} kg pro Woche",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Slider(
                value = appState.weeklyGoal,
                onValueChange = { onStateChange(appState.copy(weeklyGoal = it)) },
                valueRange = 0.1f..1.5f,
                steps = 13,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF22C55E),
                    activeTrackColor = Color(0xFF22C55E),
                    inactiveTrackColor = Color(0xFF4B5563)
                )
            )
        }

        Spacer(Modifier.height(22.dp))

        if (isWeightMaintenance) {
            WeightInputBox(
                label = "Aktuelles Gewicht",
                value = appState.currentWeight,
                placeholder = "z.B. 70",
                modifier = Modifier.fillMaxWidth(),
                onValueChange = { onStateChange(appState.copy(currentWeight = it)) }
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxWidth()) {
                WeightInputBox(
                    label = "Aktuelles Gewicht",
                    value = appState.currentWeight,
                    placeholder = "z.B. 70",
                    modifier = Modifier.weight(1f),
                    onValueChange = { onStateChange(appState.copy(currentWeight = it)) }
                )

                WeightInputBox(
                    label = "Zielgewicht",
                    value = appState.targetWeight,
                    placeholder = "z.B. 65",
                    modifier = Modifier.weight(1f),
                    onValueChange = { onStateChange(appState.copy(targetWeight = it)) }
                )
            }
        }

        Spacer(Modifier.weight(1f))

        PrimaryButton("Weiter", onContinueClick, enabled = canContinue)

        Spacer(Modifier.height(42.dp))
    }
}

@Composable
private fun GoalBox(
    text: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(86.dp)
            .background(
                if (selected) Color(0xFF22C55E) else Color(0xFF1F2937),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.Black else Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WeightInputBox(
    label: String,
    value: String,
    placeholder: String,
    modifier: Modifier,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = modifier
            .height(106.dp)
            .background(Color(0xFF1F2937), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)

        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                onValueChange(input.filter { it.isDigit() || it == ',' || it == '.' })
            },
            placeholder = { Text(placeholder, color = Color.Gray, fontSize = 12.sp) },
            suffix = { Text("kg", color = Color.White, fontSize = 12.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.White
            ),
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}