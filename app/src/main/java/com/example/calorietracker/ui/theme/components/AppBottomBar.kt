package com.example.calorietracker.ui.theme.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.example.calorietracker.Home
import com.example.calorietracker.MealTracking
import com.example.calorietracker.ProfileDetails
import com.example.calorietracker.Statistics

@Composable
fun AppBottomBar(
    currentDestination: NavDestination?,
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
        BottomItem(
            icon = "⌂",
            label = "Home",
            selected = currentDestination?.hasRoute<Home>() == true,
            onClick = onHomeClick
        )
        BottomItem(
            icon = "🍴",
            label = "Mahlzeit",
            selected = currentDestination?.hasRoute<MealTracking>() == true,
            onClick = onMealsClick
        )

        Box(
            modifier = Modifier
                .size(52.dp)
                .background(Color(0xFF22C55E), CircleShape)
                .clickable { onAddClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                color = Color.Black,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
        }

        BottomItem(
            icon = "▥",
            label = "Statistik",
            selected = currentDestination?.hasRoute<Statistics>() == true,
            onClick = onStatisticsClick
        )
        BottomItem(
            icon = "♡",
            label = "Profil",
            selected = currentDestination?.hasRoute<ProfileDetails>() == true,
            onClick = onProfileClick
        )
    }
}

@Composable
private fun BottomItem(
    icon: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) Color(0xFF22C55E) else Color.White.copy(alpha = 0.75f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(icon, color = color, fontSize = 24.sp)
        Text(label, color = color, fontSize = 10.sp)
    }
}