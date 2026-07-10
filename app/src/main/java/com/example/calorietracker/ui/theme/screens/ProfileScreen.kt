package com.example.calorietracker.ui.theme.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.calorietracker.R
import com.example.calorietracker.data.model.ActivityLevel
import com.example.calorietracker.data.model.MetabolismType
import com.example.calorietracker.data.model.WeightStrategy
import com.example.calorietracker.ui.theme.components.InputBox
import com.example.calorietracker.ui.theme.components.PrimaryButton
import com.example.calorietracker.ui.viewmodel.ProfileUiState
import com.example.calorietracker.ui.viewmodel.ProfileViewModel
import androidx.compose.ui.text.intl.Locale as ComposeLocale

enum class ProfilePage {
    OVERVIEW,
    PERSONAL_DATA,
    GOALS,
    NOTIFICATIONS,
    SETTINGS,
    HELP_SUPPORT
}

@get:StringRes
val ProfilePage.displayText: Int
    get() = when (this) {
        ProfilePage.OVERVIEW -> R.string.profile_overview
        ProfilePage.PERSONAL_DATA -> R.string.profile_personal_data
        ProfilePage.GOALS -> R.string.profile_goals
        ProfilePage.NOTIFICATIONS -> R.string.profile_notifications
        ProfilePage.SETTINGS -> R.string.profile_settings
        ProfilePage.HELP_SUPPORT -> R.string.profile_help_support
    }

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    page: ProfilePage = ProfilePage.OVERVIEW,
    onNavigate: (ProfilePage) -> Unit,
    onFastingClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onShowMessage: (String) -> Unit,
    onSetTitle: (String) -> Unit,
    onSetLoading: (Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val title = stringResource(page.displayText)

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            onShowMessage(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(title) {
        onSetTitle(title)
    }

    LaunchedEffect(uiState.loading) {
        onSetLoading(uiState.loading)
    }

    // Refresh data when entering overview to ensure consistency
    LaunchedEffect(page) {
        if (page == ProfilePage.OVERVIEW) {
            viewModel.refresh()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when (page) {
            ProfilePage.OVERVIEW -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                ProfileOverview(
                    uiState = uiState,
                    onNavigate = onNavigate,
                    onFastingClick = onFastingClick,
                    onLogout = {
                        viewModel.logout()
                        onLogoutClick()
                    },
                    onImageUpdate = { viewModel.updateProfileImage(it) }
                )
                Spacer(Modifier.height(32.dp))
            }

            ProfilePage.PERSONAL_DATA -> PersonalDataPage(viewModel)
            ProfilePage.GOALS -> GoalsPage(viewModel)

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                when (page) {
                    ProfilePage.NOTIFICATIONS -> NotificationsPage()
                    ProfilePage.SETTINGS -> SettingsPage()
                    ProfilePage.HELP_SUPPORT -> SupportPage()
                    else -> {}
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ProfileOverview(
    uiState: ProfileUiState,
    onNavigate: (ProfilePage) -> Unit,
    onFastingClick: () -> Unit,
    onLogout: () -> Unit,
    onImageUpdate: (ByteArray) -> Unit
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val displayName = uiState.displayName ?: "Nutzer"

    // Reset local preview when server update is confirmed
    LaunchedEffect(uiState.avatarUrl) {
        selectedImageUri = null
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            context.contentResolver.openInputStream(it)?.use { inputStream ->
                onImageUpdate(inputStream.readBytes())
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        // Profilbild Sektion
        Box(
            modifier = Modifier
                .size(86.dp)
                .clip(CircleShape)
                .background(Color(0xFF1F2937)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = selectedImageUri ?: uiState.avatarUrl ?: "https://via.placeholder.com/150",
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
            ProfileRow("Persönliche Daten") { onNavigate(ProfilePage.PERSONAL_DATA) }
            ProfileRow("Ziele & Kennzahlen") { onNavigate(ProfilePage.GOALS) }
            ProfileRow("Fastenintervalle") { onFastingClick() }
            ProfileRow("Benachrichtigungen") { onNavigate(ProfilePage.NOTIFICATIONS) }
            ProfileRow("Einstellungen") { onNavigate(ProfilePage.SETTINGS) }
            ProfileRow("Hilfe & Support") { onNavigate(ProfilePage.HELP_SUPPORT) }
            ProfileRow("Abmelden", Color(0xFFEF4444)) { onLogout() }
        }
    }
}

@Composable
private fun PersonalDataPage(viewModel: ProfileViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val confirmationMessage = stringResource(R.string.profile_email_update_sent)

    var name by remember(uiState.displayName) { mutableStateOf(uiState.displayName ?: "") }
    var email by remember(uiState.email) { mutableStateOf(uiState.email ?: "") }
    var metabolismType by remember(uiState.metabolismType) {
        mutableStateOf(uiState.metabolismType ?: MetabolismType.MALE)
    }
    var age by remember(uiState.age) { mutableIntStateOf(uiState.age ?: 25) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            InputBox(
                label = "Anzeigename",
                value = name,
                placeholder = "Dein Name",
                modifier = Modifier.fillMaxWidth(),
                onValueChange = { name = it ?: "" }
            )
            InputBox(
                label = "E-Mail Adresse",
                value = email,
                placeholder = "mail@beispiel.de",
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Email,
                onValueChange = { email = it ?: "" }
            )

            InputBox(
                label = "Alter",
                value = age,
                placeholder = "z.B. 25",
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Number,
                onValueChange = { age = it ?: 25 }
            )

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
                    MetabolismOption(
                        "Männlich",
                        metabolismType == MetabolismType.MALE,
                        Modifier.weight(1f)
                    ) { metabolismType = MetabolismType.MALE }

                    MetabolismOption(
                        "Weiblich",
                        metabolismType == MetabolismType.FEMALE,
                        Modifier.weight(1f)
                    ) { metabolismType = MetabolismType.FEMALE }
                }
            }

            if (uiState.successMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFF22C55E).copy(alpha = 0.15f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = uiState.successMessage!!,
                        color = Color(0xFF22C55E),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(100.dp))
        }

        PrimaryButton(
            text = "Speichern",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            onClick = {
                viewModel.updatePersonalData(name, email, metabolismType, age, confirmationMessage)
            }
        )
    }
}

@Composable
private fun MetabolismOption(
    text: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .background(
                if (selected) Color(0xFF22C55E) else Color(0xFF374151),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.Black else Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun GoalsPage(viewModel: ProfileViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var calories by remember(uiState.calorieGoal) { mutableIntStateOf(uiState.calorieGoal ?: 2000) }

    var proteinRatio by remember(uiState.proteinRatio) {
        mutableDoubleStateOf(
            (uiState.proteinRatio ?: 30).toDouble() / 100.0
        )
    }
    var carbsRatio by remember(uiState.carbsRatio) {
        mutableDoubleStateOf(
            (uiState.carbsRatio ?: 40).toDouble() / 100.0
        )
    }
    var fatRatio by remember(uiState.fatRatio) {
        mutableDoubleStateOf(
            (uiState.fatRatio ?: 30).toDouble() / 100.0
        )
    }

    var water by remember(uiState.waterGoalMl) {
        mutableDoubleStateOf(
            (uiState.waterGoalMl ?: 2500) / 1000.0
        )
    }
    var strategy by remember(uiState.weightStrategy) {
        mutableStateOf(
            uiState.weightStrategy ?: WeightStrategy.MAINTAIN_WEIGHT
        )
    }
    var activityLevel by remember(uiState.activityLevel) {
        mutableStateOf(uiState.activityLevel ?: ActivityLevel.LIGHTLY_ACTIVE)
    }
    var weeklyGoal by remember(uiState.weeklyGoal) { mutableStateOf(uiState.weeklyGoal ?: 0.5) }
    var targetWeight by remember(uiState.targetWeight) { mutableStateOf(uiState.targetWeight) }
    var currentWeight by remember(uiState.currentWeight) { mutableStateOf(uiState.currentWeight) }
    var dailyStepGoal by remember(uiState.dailyStepGoal) {
        mutableIntStateOf(
            uiState.dailyStepGoal ?: 10000
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                "Mein Ziel ist es ...",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                GoalBox(
                    "Abnehmen",
                    strategy == WeightStrategy.LOSE_WEIGHT,
                    Modifier.weight(1f)
                ) {
                    strategy = WeightStrategy.LOSE_WEIGHT
                    viewModel.recalculateCalories(WeightStrategy.LOSE_WEIGHT, weeklyGoal)
                }

                GoalBox(
                    "Muskeln\naufbauen",
                    strategy == WeightStrategy.GAIN_WEIGHT,
                    Modifier.weight(1f)
                ) {
                    strategy = WeightStrategy.GAIN_WEIGHT
                    viewModel.recalculateCalories(WeightStrategy.GAIN_WEIGHT, weeklyGoal)
                }

                GoalBox(
                    "Gewicht\nhalten",
                    strategy == WeightStrategy.MAINTAIN_WEIGHT,
                    Modifier.weight(1f)
                ) {
                    strategy = WeightStrategy.MAINTAIN_WEIGHT
                    viewModel.recalculateCalories(WeightStrategy.MAINTAIN_WEIGHT, weeklyGoal)
                }
            }

            if (strategy != WeightStrategy.MAINTAIN_WEIGHT) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1F2937), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text("Wöchentliches Ziel", color = Color.Gray, fontSize = 11.sp)
                    val weeklyGoalText =
                        String.format(ComposeLocale.current.platformLocale, "%.1f", weeklyGoal)
                    Text(
                        "$weeklyGoalText kg pro Woche",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Slider(
                        value = weeklyGoal.toFloat(),
                        onValueChange = {
                            weeklyGoal = it.toDouble()
                            viewModel.recalculateCalories(strategy, it.toDouble())
                        },
                        valueRange = 0.1f..1.0f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF22C55E),
                            activeTrackColor = Color(0xFF22C55E),
                            inactiveTrackColor = Color(0xFF374151)
                        )
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    InputBox(
                        label = "Aktuelles Gewicht (kg)",
                        value = currentWeight,
                        placeholder = "z.B. 80.0",
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Decimal,
                        onValueChange = { currentWeight = it }
                    )
                    InputBox(
                        label = "Zielgewicht (kg)",
                        value = targetWeight,
                        placeholder = "z.B. 75.0",
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Decimal,
                        onValueChange = { targetWeight = it }
                    )
                }

                // Prognose
                if (currentWeight != null && targetWeight != null && weeklyGoal > 0) {
                    val weightDiff = targetWeight!! - currentWeight!!
                    val isLogicCorrect =
                        (strategy == WeightStrategy.LOSE_WEIGHT && weightDiff < 0) ||
                                (strategy == WeightStrategy.GAIN_WEIGHT && weightDiff > 0)

                    if (isLogicCorrect) {
                        val absDiff = kotlin.math.abs(weightDiff)
                        val weeksToGoal = kotlin.math.ceil(absDiff / weeklyGoal).toInt()
                        if (weeksToGoal > 0) {
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
                                    text = "In ca. $weeksToGoal Wochen kannst du dein Ziel erreichen!",
                                    color = Color(0xFF22C55E),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFFEF4444).copy(alpha = 0.15f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val message = if (strategy == WeightStrategy.LOSE_WEIGHT)
                                "Dein Zielgewicht sollte kleiner als dein aktuelles Gewicht sein."
                            else "Dein Zielgewicht sollte größer als dein aktuelles Gewicht sein."
                            Text(
                                text = message,
                                color = Color(0xFFEF4444),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                InputBox(
                    label = "Aktuelles Gewicht (kg)",
                    value = currentWeight,
                    placeholder = "z.B. 80.0",
                    modifier = Modifier.fillMaxWidth(),
                    keyboardType = KeyboardType.Decimal,
                    onValueChange = { currentWeight = it }
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Aktivitätslevel",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                listOf(
                    ActivityLevel.SEDENTARY to "Sitzend",
                    ActivityLevel.LIGHTLY_ACTIVE to "Leicht aktiv",
                    ActivityLevel.MODERATELY_ACTIVE to "Moderat aktiv",
                    ActivityLevel.VERY_ACTIVE to "Sehr aktiv",
                    ActivityLevel.EXTREMELY_ACTIVE to "Extrem aktiv"
                ).forEach { (level, label) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(
                                if (activityLevel == level) Color(0xFF22C55E) else Color(0xFF1F2937),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                activityLevel = level
                                viewModel.recalculateCalories(strategy, weeklyGoal, level)
                            }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            label,
                            color = if (activityLevel == level) Color.Black else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            GoalSettingItem(
                label = "Tägliches Kalorienziel",
                value = if (uiState.calorieGoal == null) uiState.calculatedCalories else calories,
                recommendedValue = uiState.calculatedCalories,
                isManual = uiState.calorieGoal != null,
                onValueChange = { calories = it ?: 0 },
                onReset = {
                    viewModel.updateGoals(
                        null,
                        uiState.proteinGoal,
                        uiState.carbsGoal,
                        uiState.fatGoal,
                        uiState.proteinRatio,
                        uiState.carbsRatio,
                        uiState.fatRatio,
                        (water * 1000).toInt(),
                        strategy,
                        weeklyGoal,
                        targetWeight,
                        currentWeight,
                        activityLevel,
                        dailyStepGoal
                    )
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1F2937), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nährstoffverteilung", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Standard",
                        color = Color(0xFF22C55E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            proteinRatio = 0.30
                            carbsRatio = 0.40
                            fatRatio = 0.30
                        }
                    )
                }
                Spacer(Modifier.height(12.dp))

                val totalRatio = proteinRatio + carbsRatio + fatRatio
                val isTotalCorrect = kotlin.math.abs(totalRatio - 1.0) < 0.01

                MacroRatioSlider(
                    label = "Protein",
                    ratio = proteinRatio,
                    color = Color(0xFFEF4444),
                    onRatioChange = { proteinRatio = it }
                )
                MacroRatioSlider(
                    label = "Kohlenhydrate",
                    ratio = carbsRatio,
                    color = Color(0xFFF97316),
                    onRatioChange = { carbsRatio = it }
                )
                MacroRatioSlider(
                    label = "Fett",
                    ratio = fatRatio,
                    color = Color(0xFFFACC15),
                    onRatioChange = { fatRatio = it }
                )

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Gesamt: ${(totalRatio * 100).toInt()}%",
                        color = if (isTotalCorrect) Color(0xFF22C55E) else Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (!isTotalCorrect) {
                        Text(
                            text = "Muss 100% ergeben",
                            color = Color(0xFFEF4444),
                            fontSize = 11.sp
                        )
                    }
                }

                // Preview grams based on current calories
                val currentCals =
                    (if (uiState.calorieGoal == null) uiState.calculatedCalories else calories)
                        ?: 2000
                val recCals = uiState.calculatedCalories ?: 2000
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MacroPreview(
                        label = "P",
                        grams = (currentCals * proteinRatio / 4).toInt(),
                        recGrams = (recCals * 0.30 / 4).toInt(),
                        color = Color(0xFFEF4444)
                    )
                    MacroPreview(
                        label = "C",
                        grams = (currentCals * carbsRatio / 4).toInt(),
                        recGrams = (recCals * 0.40 / 4).toInt(),
                        color = Color(0xFFF97316)
                    )
                    MacroPreview(
                        label = "F",
                        grams = (currentCals * fatRatio / 9).toInt(),
                        recGrams = (recCals * 0.30 / 9).toInt(),
                        color = Color(0xFFFACC15)
                    )
                }
            }

            InputBox(
                label = "Schrittziel pro Tag",
                value = dailyStepGoal,
                placeholder = "z.B. 10000",
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Number,
                onValueChange = { dailyStepGoal = it ?: 10000 }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1F2937), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text("Wasserziel (Liter)", color = Color.Gray, fontSize = 11.sp)
                val waterGoalText =
                    String.format(ComposeLocale.current.platformLocale, "%.1f", water)
                Text(
                    "$waterGoalText Liter pro Tag",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Slider(
                    value = water.toFloat(),
                    onValueChange = { water = it.toDouble() },
                    valueRange = 1.0f..5.0f,
                    steps = 39,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF22C55E),
                        activeTrackColor = Color(0xFF22C55E),
                        inactiveTrackColor = Color(0xFF374151)
                    )
                )
            }

            if (uiState.successMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFF22C55E).copy(alpha = 0.15f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = uiState.successMessage!!,
                        color = Color(0xFF22C55E),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(100.dp))
        }

        val totalRatioCheck = proteinRatio + carbsRatio + fatRatio
        val canUpdate = kotlin.math.abs(totalRatioCheck - 1.0) < 0.01

        PrimaryButton(
            text = "Ziele aktualisieren",
            enabled = canUpdate,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            onClick = {
                viewModel.updateGoals(
                    calories,
                    null,
                    null,
                    null,
                    (proteinRatio * 100).toInt(),
                    (carbsRatio * 100).toInt(),
                    (fatRatio * 100).toInt(),
                    (water * 1000).toInt(),
                    strategy,
                    weeklyGoal,
                    targetWeight,
                    currentWeight,
                    activityLevel,
                    dailyStepGoal
                )
            }
        )
    }
}

@Composable
private fun GoalSettingItem(
    label: String,
    value: Int?,
    recommendedValue: Int?,
    isManual: Boolean,
    modifier: Modifier = Modifier,
    onValueChange: (Int?) -> Unit,
    onReset: () -> Unit
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.Gray,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            if (isManual) {
                Text(
                    text = "Reset",
                    color = Color(0xFF22C55E),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(end = 4.dp, bottom = 4.dp)
                        .clickable { onReset() }
                )
            }
        }
        InputBox(
            label = "",
            value = value,
            placeholder = "0",
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Number,
            onValueChange = onValueChange
        )

        recommendedValue?.let { calc ->
            Row(
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Empf: $calc",
                    color = Color(0xFF22C55E).copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
                if (isManual && value != calc) {
                    Text(
                        text = " (M)",
                        color = Color.Gray,
                        fontSize = 9.sp
                    )
                } else if (!isManual) {
                    Text(
                        text = " (Auto)",
                        color = Color(0xFF22C55E).copy(alpha = 0.6f),
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MacroRatioSlider(
    label: String,
    ratio: Double,
    color: Color,
    onRatioChange: (Double) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.Gray, fontSize = 11.sp)
            Text(
                "${(ratio * 100).toInt()}%",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = ratio.toFloat(),
            onValueChange = { onRatioChange(it.toDouble()) },
            valueRange = 0f..1f,
            steps = 19, // 5% steps
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = Color(0xFF374151)
            )
        )
    }
}

@Composable
private fun MacroPreview(label: String, grams: Int, recGrams: Int, color: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(Modifier.width(4.dp))
            Text("$label: ${grams}g", color = Color.White, fontSize = 11.sp)
        }
        Text(
            text = "Empf: ${recGrams}g",
            color = Color.Gray,
            fontSize = 9.sp,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
private fun NotificationsPage() {
    var waterRemind by remember { mutableStateOf(true) }
    var mealRemind by remember { mutableStateOf(false) }

    Column {
        Spacer(Modifier.height(8.dp))
        SettingSwitch("Wasser-Erinnerungen", waterRemind) { waterRemind = it }
        SettingSwitch("Mahlzeiten-Tracker", mealRemind) { mealRemind = it }
    }
}

@Composable
private fun SettingsPage() {
    Column {
        Spacer(Modifier.height(8.dp))
        InfoCard("Design", "Dark Mode (System)")
        InfoCard("Einheiten", "Metrisch (kg, cm)")
        InfoCard("Sprache", "Deutsch")
    }
}

@Composable
private fun SupportPage() {
    val context = LocalContext.current
    val packageInfo = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (_: Exception) {
            null
        }
    }
    val versionName = packageInfo?.versionName ?: "1.0"
    val versionCode = packageInfo?.let {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            it.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            it.versionCode.toLong()
        }
    } ?: 1

    Column {
        Spacer(Modifier.height(8.dp))
        InfoCard("Version", "$versionName Build $versionCode")
        InfoCard("Kontakt", "support@calorietracker.app")
        InfoCard("Datenschutz", "Informationen anzeigen")
    }
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

@Composable
private fun ProfileRow(text: String, color: Color = Color.White, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(text, color = color, fontSize = 14.sp)
        Text("›", color = Color.Gray, fontSize = 20.sp)
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
