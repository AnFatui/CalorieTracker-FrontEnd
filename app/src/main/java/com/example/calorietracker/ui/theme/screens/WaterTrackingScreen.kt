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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calorietracker.ui.viewmodel.WaterTrackingViewModel

@Composable
fun WaterTrackingScreen(
    onBackClick: () -> Unit,
    viewModel: WaterTrackingViewModel,
    onShowMessage: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val progress = if (uiState.waterGoalMl <= 0) 0f
    else uiState.totalAmountMl.toFloat() / uiState.waterGoalMl.toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(42.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "←",
                color = Color.White,
                fontSize = 30.sp,
                modifier = Modifier.clickable { onBackClick() })
            Spacer(Modifier.weight(1f))
            Text("Wasser", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            if (uiState.loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color(0xFF3B82F6),
                    strokeWidth = 2.dp
                )
            } else {
                Spacer(Modifier.width(24.dp))
            }
        }

        Spacer(Modifier.height(24.dp))

        Box(contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(160.dp)) {
                drawArc(
                    color = Color(0xFF1F2937),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(16.dp.toPx(), cap = StrokeCap.Round)
                )

                if (progress > 0f) {
                    drawArc(
                        color = Color(0xFF3B82F6),
                        startAngle = -90f,
                        sweepAngle = 360f * progress.coerceIn(0f, 1f),
                        useCenter = false,
                        style = Stroke(16.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("💧", fontSize = 30.sp)
                Text(
                    "${String.format("%.1f", uiState.totalAmountMl / 1000.0)} L",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "/ ${String.format("%.1f", uiState.waterGoalMl / 1000.0)} L",
                    color = Color.Gray,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WaterQuickAddButton("250ml") { viewModel.addWater(250) }
            WaterQuickAddButton("500ml") { viewModel.addWater(500) }
            WaterQuickAddButton("750ml") { viewModel.addWater(750) }
        }

        Spacer(Modifier.height(28.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF1F2937), RoundedCornerShape(18.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Heute", color = Color.White, fontWeight = FontWeight.Bold)
                if (uiState.todayLogs.isNotEmpty()) {
                    Text(
                        "Alle löschen",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        modifier = Modifier.clickable {
                            viewModel.deleteAllLogsForToday()
                        }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            if (uiState.todayLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Noch kein Wasser eingetragen.", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.todayLogs.reversed()) { log ->
                        WaterLogItem(
                            amount = log.amountMl,
                            time = log.loggedAt ?: "",
                            onDelete = { log.id?.let { viewModel.deleteLog(it) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WaterQuickAddButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(Color(0xFF1F2937), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun WaterLogItem(amount: Int, time: String, onDelete: () -> Unit) {
    val displayTime = if (time.length >= 16) time.substring(11, 16) else time

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("💧", fontSize = 18.sp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text("${amount}ml Wasser", color = Color.White, fontSize = 15.sp)
            Text(displayTime, color = Color.Gray, fontSize = 11.sp)
        }
        Spacer(Modifier.weight(1f))
        Text(
            "Löschen",
            color = Color(0xFFEF4444).copy(alpha = 0.8f),
            fontSize = 12.sp,
            modifier = Modifier.clickable { onDelete() }
        )
    }
    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
}
