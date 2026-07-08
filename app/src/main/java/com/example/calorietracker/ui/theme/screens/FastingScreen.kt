package com.example.calorietracker.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calorietracker.ui.theme.components.PrimaryButton
import com.example.calorietracker.ui.viewmodel.FastingPreset
import com.example.calorietracker.ui.viewmodel.FastingViewModel
import kotlinx.datetime.LocalTime

@Composable
fun FastingScreen(
    viewModel: FastingViewModel,
    onSetLoading: (Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState.loading) {
        onSetLoading(uiState.loading)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState.loading) {
            CircularProgressIndicator(color = Color(0xFF22C55E))
        } else {
            if (uiState.showPresets || (uiState.schedule == null && !uiState.isEditing)) {
                Text(
                    "Wähle deine Methode",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    "Intervallfasten hilft dir, deine Ziele gesund zu erreichen.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                uiState.presets.forEach { preset ->
                    PresetCard(preset) { viewModel.selectPreset(preset) }
                    Spacer(Modifier.height(12.dp))
                }
                
                if (uiState.schedule != null) {
                    TextButton(onClick = { viewModel.togglePresets() }) {
                        Text("Abbrechen", color = Color.Gray)
                    }
                }
            } else {
                FastingCard(
                    isEditing = uiState.isEditing,
                    isFasting = uiState.isFasting,
                    duration = uiState.selectedDuration,
                    startTime = uiState.selectedStartTime,
                    onEditClick = { viewModel.toggleEdit() },
                    onPresetClick = { viewModel.cancelEdit() },
                    onDurationChange = { viewModel.updateDuration(it) },
                    onStartTimeChange = { viewModel.updateStartTime(it) },
                    onSaveClick = { viewModel.saveSchedule() }
                )
                
                Spacer(Modifier.height(32.dp))
                Text(
                    "Methoden",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
                uiState.presets.forEach { preset ->
                    PresetCard(preset) { viewModel.selectPreset(preset) }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun PresetCard(preset: FastingPreset, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1F2937), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(preset.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(preset.description, color = Color.Gray, fontSize = 12.sp)
            }
            Text("→", color = Color(0xFF22C55E), fontSize = 20.sp)
        }
    }
}

@Composable
fun FastingCard(
    isEditing: Boolean,
    isFasting: Boolean,
    duration: Int,
    startTime: LocalTime,
    onEditClick: () -> Unit,
    onPresetClick: () -> Unit,
    onDurationChange: (Int) -> Unit,
    onStartTimeChange: (LocalTime) -> Unit,
    onSaveClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1F2937), RoundedCornerShape(16.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dein Fastenplan",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (!isEditing) {
                Box(
                    modifier = Modifier
                        .background(
                            if (isFasting) Color(0xFF22C55E).copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isFasting) "AKTIV" else "GEPLANT",
                        color = if (isFasting) Color(0xFF22C55E) else Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isEditing) {
            Text("Dauer: $duration Stunden", color = Color.White.copy(alpha = 0.7f))
            Slider(
                value = duration.toFloat(),
                onValueChange = { onDurationChange(it.toInt()) },
                valueRange = 12f..24f,
                steps = 11,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF22C55E),
                    activeTrackColor = Color(0xFF22C55E)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Startzeit", color = Color.White.copy(alpha = 0.7f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NumberPicker(value = startTime.hour, range = 0..23, label = "Std") {
                    onStartTimeChange(LocalTime(it, startTime.minute))
                }
                NumberPicker(value = startTime.minute, range = 0..59, label = "Min") {
                    onStartTimeChange(LocalTime(startTime.hour, it))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(text = "Speichern", onClick = onSaveClick)
            
            TextButton(onClick = onPresetClick) {
                Text("Zurücksetzen", color = Color(0xFF22C55E), fontSize = 12.sp)
            }
        } else {
            FastingInfoRow(label = "Methode", value = "${duration}:${24 - duration}")
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
            FastingInfoRow(label = "Startzeit", value = String.format("%02d:%02d Uhr", startTime.hour, startTime.minute))
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedButton(
                onClick = onEditClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF22C55E)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22C55E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Plan bearbeiten")
            }
        }
    }
}

@Composable
fun FastingInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun NumberPicker(value: Int, range: IntRange, label: String, onValueChange: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { if (value > range.first) onValueChange(value - 1) }) {
                Text("-", color = Color.White, fontSize = 20.sp)
            }
            Text(
                text = String.format("%02d", value),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(30.dp),
                textAlign = TextAlign.Center
            )
            TextButton(onClick = { if (value < range.last) onValueChange(value + 1) }) {
                Text("+", color = Color.White, fontSize = 20.sp)
            }
        }
    }
}
