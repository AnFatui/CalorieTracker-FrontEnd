package com.example.calorietracker.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.calorietracker.data.model.ActivityLevel
import com.example.calorietracker.data.model.AddWeightLogDTO
import com.example.calorietracker.data.model.MetabolismType
import com.example.calorietracker.data.model.Profile
import com.example.calorietracker.data.model.WeightStrategy
import com.example.calorietracker.data.repository.ProfileRepository
import com.example.calorietracker.data.repository.WeightRepository
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive
import java.util.Calendar
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

data class OnboardingUiState(
    override val loading: Boolean = false,
    override val error: String? = null,
    val displayName: String? = null,
    val username: String? = null,
    val isUsernameTaken: Boolean = false,
    val isCheckingUsername: Boolean = false,
    val metabolismType: MetabolismType = MetabolismType.MALE,
    val heightCm: Int? = 175,
    val age: Int? = 25,
    val weightStrategy: WeightStrategy = WeightStrategy.LOSE_WEIGHT,
    val weeklyGoal: Double = 0.5,
    val currentWeight: Double? = null,
    val targetWeight: Double? = null,
    val activityLevel: ActivityLevel = ActivityLevel.LIGHTLY_ACTIVE,
    val waterGoalLiters: Double? = 2.5,
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

    override val tag: String = OnboardingViewModel::class::simpleName.name
    private var usernameCheckJob: Job? = null
    private var userNameSetManually = false

    init {
        sessionManager.currentUserInfo?.userMetadata?.get("display_name")?.jsonPrimitive?.content
            ?.let { name -> updateDisplayName(name) }
    }

    fun updateDisplayName(name: String?) {
        if (userNameSetManually) internalUiState.update { it.copy(displayName = name) }
        else internalUiState.update { it.copy(displayName = name, username = name) }
        updateUsername(name, false)
    }

    fun updateUsername(username: String?, manuallySet: Boolean = true) {
        internalUiState.update { it.copy(username = username, isUsernameTaken = false) }

        if (username == null) {
            userNameSetManually = false
        } else {
            userNameSetManually = manuallySet
            checkUsernameAvailability(username)
        }
    }

    private fun checkUsernameAvailability(username: String) {
        internalUiState.update { it.copy(loading = true) }
        usernameCheckJob?.cancel()
        usernameCheckJob = viewModelScope.launch {
            delay(500.milliseconds) // Debounce
            internalUiState.update { it.copy(isCheckingUsername = true) }
            val isTaken = profileRepository.isUsernameTaken(username)
            internalUiState.update {
                it.copy(
                    isUsernameTaken = isTaken,
                    isCheckingUsername = false,
                    loading = false
                )
            }
        }
    }

    fun updateSex(metabolismType: MetabolismType) {
        internalUiState.update { it.copy(metabolismType = metabolismType) }
    }

    fun updateHeight(height: Int?) {
        internalUiState.update { it.copy(heightCm = height) }
    }

    fun updateAge(age: Int?) {
        internalUiState.update { it.copy(age = age) }
    }

    fun updateGoal(strategy: WeightStrategy) {
        internalUiState.update { it.copy(weightStrategy = strategy) }
    }

    fun updateWeeklyGoal(value: Double) {
        internalUiState.update { it.copy(weeklyGoal = value) }
    }

    fun updateCurrentWeight(weight: Double?) {
        internalUiState.update { it.copy(currentWeight = weight) }
    }

    fun updateTargetWeight(weight: Double?) {
        internalUiState.update { it.copy(targetWeight = weight) }
    }

    fun updateActivityLevel(level: ActivityLevel) {
        internalUiState.update { it.copy(activityLevel = level) }
    }

    fun updateWaterGoal(liters: Double?) {
        internalUiState.update { it.copy(waterGoalLiters = liters) }
    }

    @OptIn(ExperimentalTime::class)
    fun completeOnboarding(onSuccess: () -> Unit) {
        val state = internalUiState.value
        if (state.isUsernameTaken) return
        if (state.username == null) return

        tryAndLogScope {
            getUserId { userId ->
                val weight = state.currentWeight ?: 0.0
                val targetWeight = state.targetWeight ?: weight
                val height = state.heightCm
                val ageValue = state.age ?: throw IllegalArgumentException("Age cannot be null")

                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val birthYear = currentYear - ageValue

                val updatedProfile = Profile(
                    id = userId,
                    username = state.username,
                    displayName = state.displayName,
                    birthYear = birthYear,
                    metabolismType = state.metabolismType,
                    heightCm = height,
                    weightStrategy = state.weightStrategy,
                    weeklyGoal = state.weeklyGoal,
                    targetWeightKg = targetWeight,
                    calorieGoal = null, // Set to null for automatic calculation
                    proteinGoal = null,
                    carbsGoal = null,
                    fatGoal = null,
                    proteinRatio = 30, // Default 30%
                    carbsRatio = 40,   // Default 40%
                    fatRatio = 30,     // Default 30%
                    waterGoalMl = state.waterGoalLiters?.let { (it * 1000).toInt() },
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
