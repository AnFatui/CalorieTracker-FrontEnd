package com.example.calorietracker.ui.theme.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.calorietracker.ui.theme.AppUiState
import com.example.calorietracker.ui.theme.components.AppBottomBar
import com.example.calorietracker.ui.theme.components.PrimaryButton

@Composable
fun ProfileScreen(
    appState: AppUiState,
    onStateChange: (AppUiState) -> Unit,
    onHomeClick: () -> Unit,
    onMealsClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var currentProfilePage by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onStateChange(appState.copy(profileImageUri = it.toString()))
        }
    }

    if (currentProfilePage != null) {
        ProfileSubPage(
            title = currentProfilePage!!,
            appState = appState,
            onStateChange = onStateChange,
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
        Spacer(Modifier.height(64.dp))

        Box(
            modifier = Modifier
                .size(86.dp)
                .clip(CircleShape)
                .background(Color(0xFF4B5563), CircleShape)
                .clickable {
                    imagePickerLauncher.launch("image/*")
                },
            contentAlignment = Alignment.Center
        ) {
            if (appState.profileImageUri != null) {
                AsyncImage(
                    model = appState.profileImageUri,
                    contentDescription = "Profilbild",
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("Profilbild", color = Color.White, fontSize = 11.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Profilbild ändern",
            color = Color(0xFF22C55E),
            fontSize = 11.sp,
            modifier = Modifier.clickable {
                imagePickerLauncher.launch("image/*")
            }
        )

        Spacer(Modifier.height(12.dp))

        Text(
            if (appState.name.isBlank()) "Profil" else appState.name,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(28.dp))

        Column(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFF1F2937), RoundedCornerShape(18.dp))
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
    appState: AppUiState,
    onStateChange: (AppUiState) -> Unit,
    onBackClick: () -> Unit
) {
    var nameInput by remember { mutableStateOf(appState.name) }
    var emailInput by remember { mutableStateOf(appState.email) }

    var calorieGoalInput by remember { mutableStateOf(appState.calorieGoal.toString()) }
    var proteinGoalInput by remember { mutableStateOf(appState.proteinGoal.toString()) }
    var carbGoalInput by remember { mutableStateOf(appState.carbsGoal.toString()) }
    var fatGoalInput by remember { mutableStateOf(appState.fatGoal.toString()) }
    var waterGoalInput by remember { mutableStateOf((appState.waterGoalMl / 1000.0).toString()) }

    var fastingStartInput by remember { mutableStateOf(appState.fastingStartTime) }
    var fastingEndInput by remember { mutableStateOf(appState.fastingEndTime) }

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

            Text(
                title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(28.dp))

        when (title) {
            "Persönliche Daten" -> {
                EditableInput("Name", nameInput, KeyboardType.Text) {
                    nameInput = it
                }

                EditableInput("E-Mail", emailInput, KeyboardType.Email) {
                    emailInput = it
                }

                Spacer(Modifier.height(20.dp))

                PrimaryButton(
                    text = "Speichern",
                    onClick = {
                        onStateChange(
                            appState.copy(
                                name = nameInput,
                                email = emailInput
                            )
                        )
                        onBackClick()
                    }
                )
            }

            "Ziele & Kennzahlen" -> {
                InfoCard("Aktuelles Ziel", appState.selectedGoal.ifBlank { "Noch nicht ausgewählt" })

                EditableInput("Kalorienziel", calorieGoalInput, KeyboardType.Number) {
                    calorieGoalInput = it.filter { c -> c.isDigit() }
                }

                EditableInput("Proteinziel in g", proteinGoalInput, KeyboardType.Number) {
                    proteinGoalInput = it.filter { c -> c.isDigit() }
                }

                EditableInput("Kohlenhydrateziel in g", carbGoalInput, KeyboardType.Number) {
                    carbGoalInput = it.filter { c -> c.isDigit() }
                }

                EditableInput("Fettziel in g", fatGoalInput, KeyboardType.Number) {
                    fatGoalInput = it.filter { c -> c.isDigit() }
                }

                EditableInput("Wasserziel in Liter", waterGoalInput, KeyboardType.Number) {
                    waterGoalInput = it.filter { c -> c.isDigit() || c == ',' || c == '.' }
                }

                Spacer(Modifier.height(20.dp))

                PrimaryButton(
                    text = "Speichern",
                    onClick = {
                        val waterMl =
                            ((waterGoalInput.replace(",", ".").toDoubleOrNull() ?: 2.5) * 1000).toInt()

                        onStateChange(
                            appState.copy(
                                calorieGoal = calorieGoalInput.toIntOrNull() ?: appState.calorieGoal,
                                proteinGoal = proteinGoalInput.toIntOrNull() ?: appState.proteinGoal,
                                carbsGoal = carbGoalInput.toIntOrNull() ?: appState.carbsGoal,
                                fatGoal = fatGoalInput.toIntOrNull() ?: appState.fatGoal,
                                waterGoalMl = waterMl
                            )
                        )

                        onBackClick()
                    }
                )
            }

            "Fastenintervalle" -> {
                EditableInput(
                    label = "Startzeit",
                    value = fastingStartInput,
                    keyboardType = KeyboardType.Text
                ) {
                    fastingStartInput = it
                }

                EditableInput(
                    label = "Endzeit",
                    value = fastingEndInput,
                    keyboardType = KeyboardType.Text
                ) {
                    fastingEndInput = it
                }

                Spacer(Modifier.height(8.dp))

                InfoCard(
                    title = "Aktuelles Intervall",
                    value = if (fastingStartInput.isBlank() && fastingEndInput.isBlank()) {
                        "Noch nicht eingestellt"
                    } else {
                        "${fastingStartInput.ifBlank { "--:--" }} bis ${fastingEndInput.ifBlank { "--:--" }}"
                    }
                )

                Spacer(Modifier.height(20.dp))

                PrimaryButton(
                    text = "Speichern",
                    onClick = {
                        onStateChange(
                            appState.copy(
                                fastingStartTime = fastingStartInput,
                                fastingEndTime = fastingEndInput
                            )
                        )
                        onBackClick()
                    }
                )
            }

            "Benachrichtigungen" -> {
                SettingSwitch("Wasser-Erinnerung", appState.waterReminderEnabled) {
                    onStateChange(appState.copy(waterReminderEnabled = it))
                }

                SettingSwitch("Fasten-Erinnerung", appState.fastingReminderEnabled) {
                    onStateChange(appState.copy(fastingReminderEnabled = it))
                }
            }

            "Einstellungen" -> {
                InfoCard("Design", "Dark Mode")
                InfoCard("Sprache", "Deutsch")
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
        modifier = Modifier
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
private fun EditableInput(
    label: String,
    value: String,
    keyboardType: KeyboardType,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 14.dp)) {
        Text(
            label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(6.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(label, color = Color.Gray, fontSize = 12.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1F2937),
                unfocusedContainerColor = Color(0xFF1F2937),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.White
            ),
            textStyle = TextStyle(color = Color.White)
        )
    }
}

@Composable
private fun InfoCard(
    title: String,
    value: String
) {
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

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}