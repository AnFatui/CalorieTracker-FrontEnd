package com.example.calorietracker.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.calorietracker.data.model.AddWeightLogDTO
import com.example.calorietracker.data.model.Profile
import com.example.calorietracker.data.repository.ProfileRepository
import com.example.calorietracker.data.repository.WeightRepository
import com.example.calorietracker.util.CalorieCalculator
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class OnboardingUiState(
    override val loading: Boolean = false,
    override val error: String? = null,
    val displayName: String = "",
    val username: String = "",
    val isUsernameTaken: Boolean = false,
    val isCheckingUsername: Boolean = false,
    val sex: String = "Männlich",
    val heightCm: String = "175",
    val age: String = "25",
    val selectedGoal: String = "",
    val weeklyGoal: Double = 0.5,
    val currentWeight: String = "",
    val targetWeight: String = "",
    val activityLevel: String = "Moderat aktiv",
) : UiState<OnboardingUiState> {
    override fun copyFlags(
        loading: Boolean,
        error: String?
    ): OnboardingUiState {
        return this.copy(loading = loading, error = error)
    }
}

class OnboardingViewModel(
    private val profileRepository: ProfileRepository,
    private val weightRepository: WeightRepository,
    override val sessionManager: SessionManager
) : BaseViewModel<OnboardingUiState>() {
    override val internalUiState = MutableStateFlow(OnboardingUiState())
    val uiState = internalUiState.asStateFlow()

    override val tag: String = "OnboardingViewModel"
    private var usernameCheckJob: Job? = null

    fun updateDisplayName(name: String) {
        internalUiState.update { it.copy(displayName = name) }
        // Automatische Benutzernamen generieren, wenn er noch leer ist
        if (internalUiState.value.username.isEmpty()) {
            val generatedUsername =
                name.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
            updateUsername(generatedUsername)
        }
    }

    fun updateUsername(username: String) {
        val filteredUsername = username.lowercase().filter { it.isLetterOrDigit() || it == '_' }
        internalUiState.update { it.copy(username = filteredUsername, isUsernameTaken = false) }

        if (filteredUsername.length >= 3) {
            checkUsernameAvailability(filteredUsername)
        }
    }

    private fun checkUsernameAvailability(username: String) {
        usernameCheckJob?.cancel()
        usernameCheckJob = viewModelScope.launch {
            delay(500) // Debounce
            internalUiState.update { it.copy(isCheckingUsername = true) }
            val isTaken = profileRepository.isUsernameTaken(username)
            internalUiState.update {
                it.copy(
                    isUsernameTaken = isTaken,
                    isCheckingUsername = false
                )
            }
        }
    }

    fun updateSex(sex: String) {
        internalUiState.update { it.copy(sex = sex) }
    }

    fun updateHeight(height: String) {
        internalUiState.update { it.copy(heightCm = height) }
    }

    fun updateAge(age: String) {
        internalUiState.update { it.copy(age = age) }
    }

    fun updateGoal(goal: String) {
        internalUiState.update { it.copy(selectedGoal = goal) }
    }

    fun updateWeeklyGoal(value: Double) {
        internalUiState.update { it.copy(weeklyGoal = value) }
    }

    fun updateCurrentWeight(weight: String) {
        internalUiState.update { it.copy(currentWeight = weight) }
    }

    fun updateTargetWeight(weight: String) {
        internalUiState.update { it.copy(targetWeight = weight) }
    }

    fun updateActivityLevel(level: String) {
        internalUiState.update { it.copy(activityLevel = level) }
    }

    @OptIn(ExperimentalTime::class)
    fun completeOnboarding(onSuccess: () -> Unit) {
        val state = internalUiState.value
        if (state.isUsernameTaken) return

        tryAndLogScope {
            getUserId { userId ->
                val weight = state.currentWeight.toDoubleOrNull() ?: 0.0
                val targetWeight = state.targetWeight.toDoubleOrNull() ?: weight
                val height = state.heightCm.toIntOrNull() ?: 175
                val ageValue = state.age.toIntOrNull() ?: 25

                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val birthYear = currentYear - ageValue

                val calcResult = CalorieCalculator.calculate(
                    gender = state.sex,
                    age = ageValue,
                    heightCm = height,
                    weightKg = weight,
                    activityLevel = state.activityLevel,
                    goal = state.selectedGoal,
                    weeklyGoalKg = state.weeklyGoal
                )

                val updatedProfile = Profile(
                    id = userId,
                    username = state.username,
                    displayName = state.displayName,
                    birthYear = birthYear,
                    sex = state.sex,
                    heightCm = height,
                    selectedGoal = state.selectedGoal,
                    weeklyGoal = state.weeklyGoal,
                    targetWeightKg = targetWeight,
                    calorieGoal = calcResult.calories,
                    proteinGoal = calcResult.protein,
                    carbsGoal = calcResult.carbs,
                    fatGoal = calcResult.fat,
                    activityLevel = state.activityLevel,
                    onboardingDone = true
                )

                profileRepository.upsertProfile(updatedProfile)
                weightRepository.addWeightLog(
                    AddWeightLogDTO(
                        userId = userId,
                        weightKg = weight,
                        loggedAt = Clock.System.now()
                    )
                )

                onSuccess()
            }
        }
    }
}
