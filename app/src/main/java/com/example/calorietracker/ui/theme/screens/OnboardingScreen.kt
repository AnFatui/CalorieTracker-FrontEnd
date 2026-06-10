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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(36.dp))

        Text("━  •  •  •", color = Color(0xFF22C55E), fontSize = 22.sp)

        Spacer(Modifier.height(30.dp))

        Text(
            text = "Deine Ziele",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Erzähl uns etwas über deine Ziele, damit wir dich bestmöglich unterstützen können.",
            color = Color.White,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )

        Spacer(Modifier.height(34.dp))

        Text(
            text = "Mein Ziel ist es ...",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(14.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            GoalBox(
                text = "Abnehmen",
                selected = appState.selectedGoal == "Abnehmen",
                modifier = Modifier.weight(1f),
                onClick = {
                    onStateChange(appState.copy(selectedGoal = "Abnehmen"))
                }
            )

            GoalBox(
                text = "Muskeln\naufbauen",
                selected = appState.selectedGoal == "Muskeln aufbauen",
                modifier = Modifier.weight(1f),
                onClick = {
                    onStateChange(appState.copy(selectedGoal = "Muskeln aufbauen"))
                }
            )

            GoalBox(
                text = "Gewicht\nhalten",
                selected = appState.selectedGoal == "Gewicht halten",
                modifier = Modifier.weight(1f),
                onClick = {
                    onStateChange(
                        appState.copy(
                            selectedGoal = "Gewicht halten",
                            targetWeight = ""
                        )
                    )
                }
            )
        }

        if (!isWeightMaintenance) {
            Spacer(Modifier.height(28.dp))

            Text(
                text = "Mein wöchentliches Ziel",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "${String.format("%.1f", appState.weeklyGoal)} kg pro Woche",
                color = Color.White,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Slider(
                value = appState.weeklyGoal,
                onValueChange = { newValue ->
                    onStateChange(appState.copy(weeklyGoal = newValue))
                },
                valueRange = 0.1f..1.5f,
                steps = 13,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF22C55E),
                    activeTrackColor = Color(0xFF22C55E),
                    inactiveTrackColor = Color(0xFF4B5563)
                )
            )
        }

        Spacer(Modifier.height(24.dp))

        if (isWeightMaintenance) {
            WeightInputBox(
                label = "Aktuelles Gewicht",
                value = appState.currentWeight,
                placeholder = "z.B. 70",
                modifier = Modifier.fillMaxWidth(),
                onValueChange = {
                    onStateChange(appState.copy(currentWeight = it))
                }
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                WeightInputBox(
                    label = "Aktuelles Gewicht",
                    value = appState.currentWeight,
                    placeholder = "z.B. 70",
                    modifier = Modifier.weight(1f),
                    onValueChange = {
                        onStateChange(appState.copy(currentWeight = it))
                    }
                )

                WeightInputBox(
                    label = "Zielgewicht",
                    value = appState.targetWeight,
                    placeholder = "z.B. 65",
                    modifier = Modifier.weight(1f),
                    onValueChange = {
                        onStateChange(appState.copy(targetWeight = it))
                    }
                )
            }
        }

        Spacer(Modifier.weight(1f))

        PrimaryButton(
            text = "Weiter",
            onClick = onContinueClick
        )

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
                color = if (selected) Color(0xFF22C55E) else Color(0xFF1F2937),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
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
            .height(104.dp)
            .background(Color(0xFF1F2937), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp
        )

        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                onValueChange(input.filter { it.isDigit() || it == ',' || it == '.' })
            },
            placeholder = {
                Text(placeholder, color = Color.Gray, fontSize = 12.sp)
            },
            suffix = {
                Text("kg", color = Color.White, fontSize = 12.sp)
            },
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