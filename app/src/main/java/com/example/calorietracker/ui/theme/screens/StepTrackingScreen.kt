package com.example.calorietracker.ui.theme.screens

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import com.example.calorietracker.ui.theme.components.PrimaryButton
import com.example.calorietracker.ui.viewmodel.StepTrackingViewModel

@Composable
fun StepTrackingScreen(
    viewModel: StepTrackingViewModel,
    onSetLoading: (Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.loading) {
        onSetLoading(uiState.loading)
    }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    val stepsPermissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) {
        viewModel.refresh()
    }

    var goalInput by remember(uiState.stepGoal) { mutableStateOf(uiState.stepGoal?.toString() ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.size(160.dp)) {
                drawArc(
                    color = Color(0xFF1F2937),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(16.dp.toPx(), cap = StrokeCap.Round)
                )

                val goal = uiState.stepGoal
                if (goal != null && goal > 0) {
                    val progress = (uiState.steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
                    if (progress > 0f) {
                        drawArc(
                            color = Color(0xFFA855F7),
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            style = Stroke(16.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("👣", fontSize = 30.sp)
                Text(
                    "${uiState.steps}",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = uiState.stepGoal?.let { "von $it Schritten" } ?: "Schritte heute",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1F2937), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Text("Tägliches Schrittziel", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = goalInput,
                onValueChange = { input -> if (input.length <= 6 && input.all { it.isDigit() }) goalInput = input },
                singleLine = true,
                placeholder = { Text("z. B. 8000", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF111827),
                    unfocusedContainerColor = Color(0xFF111827),
                    focusedIndicatorColor = Color(0xFFA855F7),
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = TextStyle(color = Color.White, fontSize = 14.sp)
            )

            Spacer(Modifier.height(12.dp))

            PrimaryButton(
                text = "Ziel speichern",
                enabled = (goalInput.toIntOrNull() ?: 0) > 0,
                onClick = {
                    goalInput.toIntOrNull()?.let { goal ->
                        if (goal > 0) viewModel.saveStepGoal(goal)
                    }
                }
            )
        }

        Spacer(Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1F2937), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Text("Verbindung mit Health Connect", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

            Spacer(Modifier.height(8.dp))

            Text(
                text = "MacroMate zählt Schritte nicht selbst, sondern liest sie über Health Connect. Dafür muss Health Connect auf deinem Gerät installiert sein, eine Gesundheits-App wie Samsung Health muss ihre Schritte mit Health Connect synchronisieren, und MacroMate braucht die Berechtigung, diese Schritte zu lesen.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(Modifier.height(12.dp))

            when {
                !uiState.isHealthConnectAvailable -> {
                    Text(
                        text = "Health Connect ist auf diesem Gerät nicht verfügbar oder nicht installiert.",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp
                    )
                }
                !uiState.hasStepsPermission -> {
                    Text(
                        text = "MacroMate hat noch keine Berechtigung, Schritte zu lesen.",
                        color = Color(0xFFFACC15),
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton(
                        text = "Berechtigung anfragen",
                        onClick = { stepsPermissionLauncher.launch(viewModel.stepsPermissions) }
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Health-Connect-Einstellungen öffnen",
                        color = Color(0xFFA855F7),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { openHealthConnectSettings(context) }
                    )
                }
                else -> {
                    Text(
                        text = "Verbunden – Schritte werden aus Health Connect gelesen.",
                        color = Color(0xFF22C55E),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

private fun openHealthConnectSettings(context: Context) {
    try {
        context.startActivity(Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS))
    } catch (e: Exception) {
        Log.e("StepTrackingScreen", "Failed to open Health Connect settings", e)
    }
}
