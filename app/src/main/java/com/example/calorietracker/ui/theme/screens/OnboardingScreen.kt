package com.example.calorietracker.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calorietracker.ui.theme.components.PrimaryButton
import com.example.calorietracker.ui.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(
    onContinueClick: () -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: OnboardingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val isWeightMaintenance = uiState.selectedGoal == "Gewicht halten"
    val canContinue = uiState.selectedGoal.isNotBlank() &&
            uiState.currentWeight.isNotBlank() &&
            (isWeightMaintenance || uiState.targetWeight.isNotBlank()) &&
            !uiState.isLoading

    // Observe error state
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            onShowMessage(it)
            viewModel.clearError()
        }
    }

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
            GoalBox("Abnehmen", uiState.selectedGoal == "Abnehmen", Modifier.weight(1f)) {
                viewModel.updateGoal("Abnehmen")
            }

            GoalBox("Muskeln\naufbauen", uiState.selectedGoal == "Muskeln aufbauen", Modifier.weight(1f)) {
                viewModel.updateGoal("Muskeln aufbauen")
            }

            GoalBox("Gewicht\nhalten", uiState.selectedGoal == "Gewicht halten", Modifier.weight(1f)) {
                viewModel.updateGoal("Gewicht halten")
                viewModel.updateTargetWeight("")
            }
        }

        if (!isWeightMaintenance && uiState.selectedGoal.isNotBlank()) {
            Spacer(Modifier.height(28.dp))

            Text(
                "Mein wöchentliches Ziel",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                "${String.format("%.1f", uiState.weeklyGoal)} kg pro Woche",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Slider(
                value = uiState.weeklyGoal.toFloat(),
                onValueChange = { viewModel.updateWeeklyGoal(it.toDouble()) },
                valueRange = 0.1f..1.0f,
                steps = 9,
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
                value = uiState.currentWeight,
                placeholder = "z.B. 70",
                modifier = Modifier.fillMaxWidth(),
                onValueChange = { viewModel.updateCurrentWeight(it) }
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxWidth()) {
                WeightInputBox(
                    label = "Aktuelles Gewicht",
                    value = uiState.currentWeight,
                    placeholder = "z.B. 70",
                    modifier = Modifier.weight(1f),
                    onValueChange = { viewModel.updateCurrentWeight(it) }
                )

                WeightInputBox(
                    label = "Zielgewicht",
                    value = uiState.targetWeight,
                    placeholder = "z.B. 65",
                    modifier = Modifier.weight(1f),
                    onValueChange = { viewModel.updateTargetWeight(it) }
                )
            }
        }

        Spacer(Modifier.weight(1f))

        if (uiState.isLoading) {
            CircularProgressIndicator(color = Color(0xFF22C55E))
        } else {
            PrimaryButton(
                text = "Weiter", 
                onClick = { viewModel.completeOnboarding(onContinueClick) }, 
                enabled = canContinue
            )
        }

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
