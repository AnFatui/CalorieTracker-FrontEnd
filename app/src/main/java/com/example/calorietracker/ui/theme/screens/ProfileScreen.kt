package com.example.calorietracker.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calorietracker.ui.theme.components.AppBottomBar

@Composable
fun ProfileScreen(
    onHomeClick: () -> Unit,
    onMealsClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var currentProfilePage by remember { mutableStateOf<String?>(null) }

    if (currentProfilePage != null) {
        ProfileSubPage(
            title = currentProfilePage!!,
            onBackClick = { currentProfilePage = null }
        )
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(64.dp))

        Box(
            Modifier
                .size(86.dp)
                .background(Color(0xFF4B5563), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("Profilbild", color = Color.White, fontSize = 11.sp)
        }

        Spacer(Modifier.height(18.dp))

        Text("Profil", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(28.dp))

        Column(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFF1F2937), RoundedCornerShape(16.dp))
                .padding(vertical = 8.dp)
        ) {
            ProfileRow("Persönliche Daten") { currentProfilePage = "Persönliche Daten" }
            ProfileRow("Ziele & Kennzahlen") { currentProfilePage = "Ziele & Kennzahlen" }
            ProfileRow("Fastenintervalle") { currentProfilePage = "Fastenintervalle" }
            ProfileRow("Benachrichtigungen") { currentProfilePage = "Benachrichtigungen" }
            ProfileRow("Einstellungen") { currentProfilePage = "Einstellungen" }
            ProfileRow("Hilfe & Support") { currentProfilePage = "Hilfe & Support" }
            ProfileRow("Abmelden", Color(0xFFEF4444)) { onLogoutClick() }
        }

        Spacer(Modifier.weight(1f))

        AppBottomBar(
            selected = "Profile",
            onHomeClick = onHomeClick,
            onMealsClick = onMealsClick,
            onAddClick = onMealsClick,
            onStatisticsClick = onStatisticsClick,
            onProfileClick = {}
        )
    }
}

@Composable
private fun ProfileSubPage(
    title: String,
    onBackClick: () -> Unit
) {
    var switchValue by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(42.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "←",
                color = Color.White,
                fontSize = 30.sp,
                modifier = Modifier.clickable { onBackClick() }
            )

            Spacer(Modifier.width(18.dp))

            Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(28.dp))

        when (title) {
            "Persönliche Daten" -> {
                InfoCard("Name", "Noch nicht eingetragen")
                InfoCard("E-Mail", "Wird später mit Login verbunden")
                InfoCard("Geburtsdatum", "Noch nicht eingetragen")
            }

            "Ziele & Kennzahlen" -> {
                InfoCard("Kalorienziel", "2000 kcal")
                InfoCard("Proteinziel", "130 g")
                InfoCard("Wasserziel", "2,5 L")
            }

            "Fastenintervalle" -> {
                InfoCard("Aktuelles Intervall", "Noch nicht eingestellt")
                InfoCard("Startzeit", "--:--")
                InfoCard("Endzeit", "--:--")
            }

            "Benachrichtigungen" -> {
                SettingSwitch("Wasser-Erinnerung", switchValue) { switchValue = it }
                SettingSwitch("Fasten-Erinnerung", switchValue) { switchValue = it }
            }

            "Einstellungen" -> {
                InfoCard("Design", "Dark Mode")
                InfoCard("Sprache", "Deutsch")
                InfoCard("Datenbank", "Wird später angebunden")
            }

            "Hilfe & Support" -> {
                InfoCard("FAQ", "Häufige Fragen")
                InfoCard("Kontakt", "Support wird später ergänzt")
            }
        }
    }
}

@Composable
private fun ProfileRow(
    text: String,
    color: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, color = color, fontSize = 12.sp)
        Text(">", color = Color.White, fontSize = 12.sp)
    }
}

@Composable
private fun InfoCard(title: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
            .background(Color(0xFF1F2937), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(value, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
            .background(Color(0xFF1F2937), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}