package com.example.calorietracker.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.calorietracker.ui.theme.components.PrimaryButton
import com.example.calorietracker.ui.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(
    onContinueClick: () -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: OnboardingViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentStep by remember { mutableIntStateOf(1) }

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
            .padding(horizontal = 28.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(36.dp))

        // Progress Indicator
        Text(
            if (currentStep == 1) "━  •" else "•  ━",
            color = Color(0xFF22C55E),
            fontSize = 22.sp
        )

        Spacer(Modifier.height(28.dp))

        if (currentStep == 1) {
            StepOnePersonalInfo(uiState, viewModel)
        } else {
            StepTwoGoalsAndWeight(uiState, viewModel)
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))

        if (uiState.loading) {
            CircularProgressIndicator(color = Color(0xFF22C55E))
        } else {
            PrimaryButton(
                text = if (currentStep == 1) "Weiter" else "Fertigstellen",
                onClick = {
                    if (currentStep == 1) {
                        currentStep = 2
                    } else {
                        viewModel.completeOnboarding(onContinueClick)
                    }
                },
                enabled = canNavigateNext(currentStep, uiState)
            )
        }

        if (currentStep == 2) {
            TextButton(onClick = { currentStep = 1 }) {
                Text("Zurück", color = Color.Gray)
            }
        }

        Spacer(Modifier.height(42.dp))
    }
}

private fun canNavigateNext(step: Int, state: com.example.calorietracker.ui.viewmodel.OnboardingUiState): Boolean {
    return if (step == 1) {
        state.displayName.isNotBlank() && 
        state.username.isNotBlank() && 
        !state.isUsernameTaken && 
        state.age.isNotBlank() && 
        state.heightCm.isNotBlank()
    } else {
        val isWeightMaintenance = state.selectedGoal == "Gewicht halten"
        state.selectedGoal.isNotBlank() &&
                state.currentWeight.isNotBlank() &&
                (isWeightMaintenance || state.targetWeight.isNotBlank())
    }
}

@Composable
private fun StepOnePersonalInfo(
    uiState: com.example.calorietracker.ui.viewmodel.OnboardingUiState,
    viewModel: OnboardingViewModel
) {
    Text("Über dich", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)

    Text(
        text = "Lass uns dich kurz kennenlernen, um deinen Bedarf zu ermitteln.",
        color = Color.White.copy(alpha = 0.8f),
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 12.dp)
    )

    Spacer(Modifier.height(32.dp))

    InputBox(
        label = "Anzeigename",
        value = uiState.displayName,
        placeholder = "z.B. Max Mustermann",
        modifier = Modifier.fillMaxWidth(),
        onValueChange = { viewModel.updateDisplayName(it) }
    )

    Spacer(Modifier.height(20.dp))

    InputBox(
        label = "Benutzername",
        value = uiState.username,
        placeholder = "z.B. max_m",
        modifier = Modifier.fillMaxWidth(),
        isError = uiState.isUsernameTaken,
        helperText = if (uiState.isUsernameTaken) "Dieser Name ist bereits vergeben" else "Wird für den Login benötigt",
        onValueChange = { viewModel.updateUsername(it) }
    )

    Spacer(Modifier.height(20.dp))

    Text(
        "Geschlecht",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(14.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        GoalBox("Männlich", uiState.sex == "Männlich", Modifier.weight(1f)) {
            viewModel.updateSex("Männlich")
        }
        GoalBox("Weiblich", uiState.sex == "Weiblich", Modifier.weight(1f)) {
            viewModel.updateSex("Weiblich")
        }
    }

    Spacer(Modifier.height(20.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxWidth()) {
        InputBox(
            label = "Alter",
            value = uiState.age,
            placeholder = "25",
            modifier = Modifier.weight(1f),
            keyboardType = KeyboardType.Number,
            onValueChange = { viewModel.updateAge(it.filter { c -> c.isDigit() }) }
        )

        InputBox(
            label = "Größe (cm)",
            value = uiState.heightCm,
            placeholder = "175",
            modifier = Modifier.weight(1f),
            keyboardType = KeyboardType.Number,
            onValueChange = { viewModel.updateHeight(it.filter { c -> c.isDigit() }) }
        )
    }

    Spacer(Modifier.height(20.dp))

    Text(
        "Aktivitätslevel",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(14.dp))

    val activityLevels = listOf("Wenig aktiv", "Moderat aktiv", "Sehr aktiv")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        activityLevels.forEach { level ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .background(
                        if (uiState.activityLevel == level) Color(0xFF22C55E) else Color(0xFF1F2937),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { viewModel.updateActivityLevel(level) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    level,
                    color = if (uiState.activityLevel == level) Color.Black else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun StepTwoGoalsAndWeight(
    uiState: com.example.calorietracker.ui.viewmodel.OnboardingUiState,
    viewModel: OnboardingViewModel
) {
    val isWeightMaintenance = uiState.selectedGoal == "Gewicht halten"

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
        InputBox(
            label = "Aktuelles Gewicht",
            value = uiState.currentWeight,
            placeholder = "z.B. 70",
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Number,
            onValueChange = { viewModel.updateCurrentWeight(it.replace(',', '.')) }
        )
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxWidth()) {
            InputBox(
                label = "Aktuelles Gewicht",
                value = uiState.currentWeight,
                placeholder = "z.B. 70",
                modifier = Modifier.weight(1f),
                keyboardType = KeyboardType.Number,
                onValueChange = { viewModel.updateCurrentWeight(it.replace(',', '.')) }
            )

            InputBox(
                label = "Zielgewicht",
                value = uiState.targetWeight,
                placeholder = "z.B. 65",
                modifier = Modifier.weight(1f),
                keyboardType = KeyboardType.Number,
                onValueChange = { viewModel.updateTargetWeight(it.replace(',', '.')) }
            )
        }
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
private fun InputBox(
    label: String,
    value: String,
    placeholder: String,
    modifier: Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    helperText: String? = null,
    onValueChange: (String) -> Unit
) {
    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (isError) Color(0xFF450a0a) else Color(0xFF1F2937),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                label,
                color = if (isError) Color(0xFFef4444) else Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp
            )

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder, color = Color.Gray, fontSize = 12.sp) },
                singleLine = true,
                isError = isError,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.White,
                    errorIndicatorColor = Color.Transparent,
                    errorContainerColor = Color.Transparent
                ),
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        
        if (helperText != null) {
            Text(
                text = helperText,
                color = if (isError) Color(0xFFef4444) else Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }
}
