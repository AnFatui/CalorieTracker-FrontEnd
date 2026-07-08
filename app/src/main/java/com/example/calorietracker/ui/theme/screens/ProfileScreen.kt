package com.example.calorietracker.ui.theme.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.calorietracker.ui.theme.components.PrimaryButton
import com.example.calorietracker.ui.viewmodel.ProfileUiState
import com.example.calorietracker.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogoutClick: () -> Unit,
    onSetLoading: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var currentProfilePage by remember { mutableStateOf<String?>(null) }
    val uiState by viewModel.uiState.collectAsState()
    val displayName = uiState.displayName ?: "None"

    LaunchedEffect(uiState.loading) {
        onSetLoading(uiState.loading)
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { inputStream ->
                val bytes = inputStream.readBytes()
                viewModel.updateProfileImage(bytes)
            }
        }
    }

    if (currentProfilePage != null) {
        ProfileSubPageWrapper(
            title = currentProfilePage!!,
            uiState = uiState,
            onBackClick = { currentProfilePage = null }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profilbild Sektion
        Box(
            modifier = Modifier
                .size(86.dp)
                .clip(CircleShape)
                .background(Color(0xFF1F2937)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = uiState.avatarUrl ?: "https://via.placeholder.com/150",
                contentDescription = "Profilbild",
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = "Profilbild ändern",
            color = Color(0xFF22C55E),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { imagePickerLauncher.launch("image/*") }
        )

        Spacer(Modifier.height(12.dp))
        Text(
            text = displayName,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(32.dp))

        // Menü-Liste
        Column(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFF1F2937), RoundedCornerShape(24.dp))
                .padding(vertical = 8.dp)
        ) {
            ProfileRow("Persönliche Daten") { currentProfilePage = "Persönliche Daten" }
            ProfileRow("Ziele & Kennzahlen") { currentProfilePage = "Ziele & Kennzahlen" }
            ProfileRow("Fastenintervalle") { currentProfilePage = "Fastenintervalle" }
            ProfileRow("Benachrichtigungen") { currentProfilePage = "Benachrichtigungen" }
            ProfileRow("Einstellungen") { currentProfilePage = "Einstellungen" }
            ProfileRow("Hilfe & Support") { currentProfilePage = "Hilfe & Support" }
            ProfileRow("Abmelden", Color(0xFFEF4444)) {
                viewModel.logout()
                onLogoutClick()
            }
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun ProfileSubPageWrapper(
    title: String,
    uiState: ProfileUiState,
    onBackClick: () -> Unit
) {
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
                fontSize = 28.sp,
                modifier = Modifier.clickable { onBackClick() }
            )
            Spacer(Modifier.width(16.dp))
            Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(24.dp))

        when (title) {
            "Persönliche Daten" -> PersonalDataPage(uiState, onBackClick)
            "Ziele & Kennzahlen" -> GoalsPage(uiState, onBackClick)
            "Fastenintervalle" -> FastingPage(onBackClick)
            "Benachrichtigungen" -> NotificationsPage()
            "Einstellungen" -> SettingsPage()
            "Hilfe & Support" -> SupportPage()
        }
    }
}

@Composable
private fun PersonalDataPage(
    uiState: ProfileUiState,
    onBackClick: () -> Unit
) {
    var name by remember { mutableStateOf(uiState.displayName ?: "None") }
    var email by remember { mutableStateOf("beispiel@mail.de") }

    Column {
        EditableInput("Anzeigename", name) { name = it }
        EditableInput("E-Mail Adresse", email, KeyboardType.Email) { email = it }
        Spacer(Modifier.height(24.dp))
        PrimaryButton(text = "Speichern", onClick = onBackClick)
    }
}

@Composable
private fun GoalsPage(
    uiState: ProfileUiState,
    onBackClick: () -> Unit
) {
    var calories by remember { mutableStateOf(uiState.calorieGoal?.toString() ?: "2000") }
    var water by remember { mutableStateOf("2.5") }

    Column {
        InfoCard(
            "Aktuelles Ziel",
            uiState.weightStrategy ?: "Gewicht halten"
        )
        EditableInput("Tägliches Kalorienziel", calories, KeyboardType.Number) { calories = it }
        EditableInput("Wasserziel (Liter)", water, KeyboardType.Decimal) { water = it }
        Spacer(Modifier.height(24.dp))
        PrimaryButton(text = "Ziele aktualisieren", onClick = onBackClick)
    }
}

@Composable
private fun FastingPage(onBackClick: () -> Unit) {
    var start by remember { mutableStateOf("20:00") }
    var end by remember { mutableStateOf("12:00") }

    Column {
        EditableInput("Fasten-Beginn", start) { start = it }
        EditableInput("Fasten-Ende", end) { end = it }
        InfoCard("Intervall", "16:8 Methode")
        Spacer(Modifier.height(24.dp))
        PrimaryButton(text = "Intervall speichern", onClick = onBackClick)
    }
}

@Composable
private fun NotificationsPage() {
    var waterRemind by remember { mutableStateOf(true) }
    var mealRemind by remember { mutableStateOf(false) }

    Column {
        SettingSwitch("Wasser-Erinnerungen", waterRemind) { waterRemind = it }
        SettingSwitch("Mahlzeiten-Tracker", mealRemind) { mealRemind = it }
    }
}

@Composable
private fun SettingsPage() {
    Column {
        InfoCard("Design", "Dark Mode (System)")
        InfoCard("Einheiten", "Metrisch (kg, cm)")
        InfoCard("Sprache", "Deutsch")
    }
}

@Composable
private fun SupportPage() {
    Column {
        InfoCard("Version", "1.0.0 Build 42")
        InfoCard("Kontakt", "support@calorietracker.app")
        InfoCard("Datenschutz", "Informationen anzeigen")
    }
}

@Composable
private fun ProfileRow(text: String, color: Color = Color.White, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, color = color, fontSize = 14.sp)
        Text("›", color = Color.Gray, fontSize = 20.sp)
    }
}

@Composable
private fun EditableInput(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            label,
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1F2937),
                unfocusedContainerColor = Color(0xFF1F2937),
                focusedIndicatorColor = Color(0xFF22C55E),
                unfocusedIndicatorColor = Color.Transparent
            ),
            textStyle = TextStyle(color = Color.White, fontSize = 14.sp)
        )
    }
}

@Composable
private fun InfoCard(title: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .background(Color(0xFF1F2937), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(title, color = Color.Gray, fontSize = 11.sp)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SettingSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .background(Color(0xFF1F2937), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontSize = 14.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF22C55E))
        )
    }
}
