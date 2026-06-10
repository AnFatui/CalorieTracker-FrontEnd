package com.example.calorietracker.ui.theme.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppBottomBar(
    selected: String,
    onHomeClick: () -> Unit,
    onMealsClick: () -> Unit,
    onAddClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomItem("⌂", "Home", selected == "Home", onHomeClick)
        BottomItem("🍴", "Mahlzeit", selected == "Meals", onMealsClick)

        Text(
            text = "+",
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onAddClick() }
        )

        BottomItem("▥", "Statistik", selected == "Statistics", onStatisticsClick)
        BottomItem("♡", "Profil", selected == "Profile", onProfileClick)
    }
}

@Composable
private fun BottomItem(
    icon: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) Color(0xFF22C55E) else Color.White

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(icon, color = color, fontSize = 26.sp)
        Text(label, color = color, fontSize = 10.sp)
    }
}