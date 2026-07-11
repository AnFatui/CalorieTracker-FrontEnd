package com.example.calorietracker.ui.theme.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calorietracker.data.model.ActivityLevel
import com.example.calorietracker.data.model.MetabolismType
import com.example.calorietracker.data.model.WeightStrategy
import com.example.calorietracker.ui.theme.components.InputBox
import com.example.calorietracker.ui.theme.components.PrimaryButton
import com.example.calorietracker.ui.viewmodel.OnboardingUiState
import com.example.calorietracker.ui.viewmodel.OnboardingViewModel
import androidx.compose.ui.text.intl.Locale as ComposeLocale

@Composable
fun OnboardingScreen(
    onContinueClick: () -> Unit,
    onShowMessage: (String) -> Unit,
    viewModel: OnboardingViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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

        Spacer(Modifier.height(32.dp))
    }
}

private fun canNavigateNext(
    step: Int,
    state: OnboardingUiState
): Boolean {
    return if (step == 1) {
        !state.displayName.isNullOrBlank() &&
                !state.username.isNullOrBlank() &&
                !state.isUsernameTaken &&
                (state.age ?: 0) > 0 &&
                (state.heightCm ?: 0) > 0
    } else {
        val isWeightMaintenance = state.weightStrategy == WeightStrategy.MAINTAIN_WEIGHT
        val currentWeightValid = (state.currentWeight ?: 0.0) > 0.0
        val targetWeightValid = isWeightMaintenance || (state.targetWeight ?: 0.0) > 0.0
        val waterGoalValid = (state.waterGoalLiters ?: 0.0) in 0.5..10.0

        currentWeightValid && targetWeightValid && waterGoalValid
    }
}

@Composable
private fun StepOnePersonalInfo(
    uiState: OnboardingUiState,
    viewModel: OnboardingViewModel
) {
    Text("Über dich", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)

    Text(
        text = "Lass uns dich kurz kennenlernen.",
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
        helperText = if (uiState.isUsernameTaken) "Dieser Name ist bereits vergeben" else null,
        onValueChange = { viewModel.updateUsername(it) }
    )

    Spacer(Modifier.height(20.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1F2937), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Text(
            "Metabolismus",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp
        )

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .background(
                        if (uiState.metabolismType == MetabolismType.MALE) Color(0xFF22C55E) else Color(
                            0xFF374151
                        ),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { viewModel.updateSex(MetabolismType.MALE) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Männlich",
                    color = if (uiState.metabolismType == MetabolismType.MALE) Color.Black else Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .background(
                        if (uiState.metabolismType == MetabolismType.FEMALE) Color(0xFF22C55E) else Color(
                            0xFF374151
                        ),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { viewModel.updateSex(MetabolismType.FEMALE) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Weiblich",
                    color = if (uiState.metabolismType == MetabolismType.FEMALE) Color.Black else Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
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
            onValueChange = { viewModel.updateAge(it) },
            validator = { if (it != null && it <= 0) "Ungültig" else null }
        )

        InputBox(
            label = "Größe (cm)",
            value = uiState.heightCm,
            placeholder = "175",
            modifier = Modifier.weight(1f),
            keyboardType = KeyboardType.Number,
            onValueChange = { viewModel.updateHeight(it) },
            validator = { if (it != null && it <= 0) "Ungültig" else null }
        )
    }
}

@Composable
private fun StepTwoGoalsAndWeight(
    uiState: OnboardingUiState,
    viewModel: OnboardingViewModel
) {
    val isWeightMaintenance = uiState.weightStrategy == WeightStrategy.MAINTAIN_WEIGHT

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
        GoalBox(
            "Abnehmen",
            uiState.weightStrategy == WeightStrategy.LOSE_WEIGHT,
            Modifier.weight(1f)
        ) {
            viewModel.updateGoal(WeightStrategy.LOSE_WEIGHT)
        }

        GoalBox(
            "Muskeln\naufbauen",
            uiState.weightStrategy == WeightStrategy.GAIN_WEIGHT,
            Modifier.weight(1f)
        ) {
            viewModel.updateGoal(WeightStrategy.GAIN_WEIGHT)
        }

        GoalBox(
            "Gewicht\nhalten",
            uiState.weightStrategy == WeightStrategy.MAINTAIN_WEIGHT,
            Modifier.weight(1f)
        ) {
            viewModel.updateGoal(WeightStrategy.MAINTAIN_WEIGHT)
            viewModel.updateTargetWeight(null)
        }
    }

    if (!isWeightMaintenance) {
        Spacer(Modifier.height(28.dp))

        Text(
            "Mein wöchentliches Ziel",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        val weeklyGoalText =
            String.format(ComposeLocale.current.platformLocale, "%.1f", uiState.weeklyGoal)
        Text(
            "$weeklyGoalText kg pro Woche",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Slider(
            value = uiState.weeklyGoal.toFloat(),
            onValueChange = { viewModel.updateWeeklyGoal(it.toDouble()) },
            valueRange = 0.1f..1.0f,
            steps = 8,
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
            keyboardType = KeyboardType.Decimal,
            onValueChange = { viewModel.updateCurrentWeight(it) },
            validator = { weight ->
                if (weight != null && (weight !in 30.0..300.0)) "Ungültig" else null
            }
        )
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            InputBox(
                label = "Aktuelles Gewicht",
                value = uiState.currentWeight,
                placeholder = "z.B. 70",
                modifier = Modifier.weight(1f),
                keyboardType = KeyboardType.Decimal,
                onValueChange = { viewModel.updateCurrentWeight(it) },
                validator = { weight ->
                    if (weight != null && (weight !in 30.0..300.0)) "Ungültig" else null
                }
            )

            InputBox(
                label = "Zielgewicht",
                value = uiState.targetWeight,
                placeholder = "z.B. 65",
                modifier = Modifier.weight(1f),
                keyboardType = KeyboardType.Decimal,
                onValueChange = { viewModel.updateTargetWeight(it) },
                validator = { weight ->
                    if (weight != null && (weight !in 30.0..300.0)) "Ungültig" else null
                }
            )
        }
    }

    // Prognose für das Zielgewicht
    if (!isWeightMaintenance) {
        val current = uiState.currentWeight
        val target = uiState.targetWeight

        if (current != null && target != null) {
            val weightDiff = kotlin.math.abs(target - current)
            val weeksToGoal = if (uiState.weeklyGoal > 0 && weightDiff > 0) {
                kotlin.math.ceil(weightDiff / uiState.weeklyGoal).toInt()
            } else null

            weeksToGoal?.let { weeks ->
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFF22C55E).copy(alpha = 0.15f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "In ca. $weeks Wochen kannst du dein Ziel erreichen!",
                        color = Color(0xFF22C55E),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    Spacer(Modifier.height(32.dp))

    Text(
        "Dein Aktivitätslevel",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(14.dp))

    val activityLevels = listOf(
        ActivityLevel.SEDENTARY to "Sitzend (Bürojob / wenig Bewegung)",
        ActivityLevel.LIGHTLY_ACTIVE to "Leicht aktiv (viel Stehen / Gehen)",
        ActivityLevel.MODERATELY_ACTIVE to "Moderat aktiv (viel Bewegung)",
        ActivityLevel.VERY_ACTIVE to "Sehr aktiv (körperliche Arbeit)",
        ActivityLevel.EXTREMELY_ACTIVE to "Extrem aktiv (Leistungssport)"
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        activityLevels.forEach { (level, label) ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .background(
                        if (uiState.activityLevel == level) Color(0xFF22C55E) else Color(0xFF1F2937),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { viewModel.updateActivityLevel(level) }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    label,
                    color = if (uiState.activityLevel == level) Color.Black else Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    Spacer(Modifier.height(32.dp))

    Text(
        "Dein tägliches Wasserziel",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth()
    )

    val waterGoalText =
        String.format(ComposeLocale.current.platformLocale, "%.1f", uiState.waterGoalLiters ?: 2.5)
    Text(
        "$waterGoalText Liter pro Tag",
        color = Color.White.copy(alpha = 0.85f),
        fontSize = 13.sp,
        modifier = Modifier.fillMaxWidth()
    )

    Slider(
        value = (uiState.waterGoalLiters ?: 2.5).toFloat(),
        onValueChange = { viewModel.updateWaterGoal(it.toDouble()) },
        valueRange = 1.0f..5.0f,
        steps = 39, // (5.0 - 1.0) / 0.1 = 40 Intervalle, also 39 Steps dazwischen
        colors = SliderDefaults.colors(
            thumbColor = Color(0xFF22C55E),
            activeTrackColor = Color(0xFF22C55E),
            inactiveTrackColor = Color(0xFF1F2937)
        )
    )
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
