package com.example.calorietracker.ui.theme.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calorietracker.ui.viewmodel.WeightTrackingViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun WeightTrackingScreen(
    viewModel: WeightTrackingViewModel
) {
    val dataState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                WeightAdjustButton("-") { viewModel.adjustWeight(-0.1) }

                Spacer(Modifier.width(24.dp))

                val weightDisplay =
                    dataState.currentWeightKg?.let { "%.1f".format(it) } ?: "--.-"
                Text(
                    text = "$weightDisplay kg",
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.width(24.dp))

                WeightAdjustButton("+") { viewModel.adjustWeight(0.1) }
            }

            Text(
                text = "Zielgewicht: ${dataState.targetWeightKg ?: "-"} kg",
                color = Color(0xFF22C55E),
                fontSize = 14.sp
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color(0xFF050505), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (dataState.weightLogs.isEmpty()) {
                        Text("Noch kein Verlauf vorhanden.", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        Canvas(
                            Modifier
                                .fillMaxSize()
                                .padding(18.dp)
                        ) {
                            // TODO: Draw Graph logic
                        }
                    }
                }
            }

            item {
                Text(
                    "Verlauf",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (dataState.weightLogs.isEmpty()) {
                item {
                    Text("Keine Einträge gefunden.", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                val logs = dataState.weightLogs.sortedByDescending { it.loggedAt }
                itemsIndexed(logs) { index, log ->
                    val shape = when {
                        logs.size == 1 -> RoundedCornerShape(16.dp)
                        index == 0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        index == logs.size - 1 -> RoundedCornerShape(
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        )

                        else -> RectangleShape
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1F2937), shape)
                            .padding(horizontal = 14.dp)
                    ) {
                        WeightRow(
                            date = log.loggedAt.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString(),
                            weight = "${log.weightKg} kg"
                        )
                    }
                }
            }

            // Kleiner Puffer am Ende der Liste
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun WeightAdjustButton(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(Color(0xFF1F2937), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun WeightRow(date: String, weight: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(date, color = Color.White, fontSize = 14.sp)
        Text(weight, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}