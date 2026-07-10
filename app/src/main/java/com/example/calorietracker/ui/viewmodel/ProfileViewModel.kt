package com.example.calorietracker.ui.viewmodel

import com.example.calorietracker.data.model.WeightStrategy
import com.example.calorietracker.data.repository.ProfileRepository
import com.example.calorietracker.data.repository.WeightRepository
import com.example.calorietracker.util.CalorieCalculator
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class ProfileUiState(
    override val loading: Boolean = false,
    override val error: String? = null,
    val successMessage: String? = null,
    val calorieGoal: Int? = null,
    val calculatedCalories: Int? = null,
    val proteinGoal: Int? = null,
    val calculatedProtein: Int? = null,
    val proteinRatio: Int? = null,
    val carbsGoal: Int? = null,
    val calculatedCarbs: Int? = null,
    val carbsRatio: Int? = null,
    val fatGoal: Int? = null,
    val calculatedFat: Int? = null,
    val fatRatio: Int? = null,
    val waterGoalMl: Int? = null,
    val weightStrategy: WeightStrategy? = null,
    val weeklyGoal: Double? = null,
    val targetWeight: Double? = null,
    val currentWeight: Double? = null,
    val metabolismType: com.example.calorietracker.data.model.MetabolismType? = null,
    val age: Int? = null,
    val activityLevel: com.example.calorietracker.data.model.ActivityLevel? = null,
    val dailyStepGoal: Int? = null,
    val avatarUrl: String? = null,
    val displayName: String? = null,
    val email: String? = null,
) : UiState<ProfileUiState> {
    override fun copyFlags(
        loading: Boolean,
        error: String?
    ): ProfileUiState {
        return this.copy(error = error, loading = loading, successMessage = if (loading) null else successMessage)
    }
}

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val weightRepository: WeightRepository,
    override val sessionManager: SessionManager,
) : BaseViewModel<ProfileUiState>() {
    override val internalUiState = MutableStateFlow(ProfileUiState())
    override val tag: String = "ProfileViewModel"

    val uiState: StateFlow<ProfileUiState> = internalUiState.asStateFlow()

    init {
        loadData()
    }

    fun refresh() {
        loadData()
    }

    private fun loadData() {
        tryAndLogScope {
            val currentUserId = sessionManager.currentUserId;
            if (currentUserId == null) {
                internalUiState.update { it.copy(error = "Failed, no user id found") }
                return@tryAndLogScope
            }

            val profile = profileRepository.getProfile(currentUserId)
            if (profile == null) {
                internalUiState.update { it.copy(error = "Failed to load profile") }
                return@tryAndLogScope
            }

            // Calculate recommended calories and macros
            val latestWeight = weightRepository.getLatestWeightLog(currentUserId)?.weightKg
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val age = profile.birthYear?.let { currentYear - it } ?: 25
            
            val calcResult = if (latestWeight != null && profile.heightCm != null && profile.activityLevel != null && profile.weightStrategy != null) {
                CalorieCalculator.calculate(
                    metabolismType = profile.metabolismType,
                    age = age,
                    heightCm = profile.heightCm,
                    weightKg = latestWeight,
                    activityLevel = profile.activityLevel,
                    weightStrategy = profile.weightStrategy,
                    weeklyGoalKg = profile.weeklyGoal ?: 0.5
                )
            } else null

            // Override calcResult macros if user has custom ratios
            val finalProtein = if (calcResult != null && profile.calorieGoal != null && profile.proteinRatio != null) 
                (profile.calorieGoal * (profile.proteinRatio / 100.0) / 4).toInt() else calcResult?.protein
            val finalCarbs = if (calcResult != null && profile.calorieGoal != null && profile.carbsRatio != null) 
                (profile.calorieGoal * (profile.carbsRatio / 100.0) / 4).toInt() else calcResult?.carbs
            val finalFat = if (calcResult != null && profile.calorieGoal != null && profile.fatRatio != null) 
                (profile.calorieGoal * (profile.fatRatio / 100.0) / 9).toInt() else calcResult?.fat

            internalUiState.update {
                it.copy(
                    displayName = profile.displayName,
                    calorieGoal = profile.calorieGoal,
                    calculatedCalories = calcResult?.calories,
                    proteinGoal = profile.proteinGoal,
                    calculatedProtein = finalProtein,
                    proteinRatio = profile.proteinRatio,
                    carbsGoal = profile.carbsGoal,
                    calculatedCarbs = finalCarbs,
                    carbsRatio = profile.carbsRatio,
                    fatGoal = profile.fatGoal,
                    calculatedFat = finalFat,
                    fatRatio = profile.fatRatio,
                    waterGoalMl = profile.waterGoalMl,
                    avatarUrl = profile.avatarUrl,
                    email = profile.email,
                    weightStrategy = profile.weightStrategy,
                    weeklyGoal = profile.weeklyGoal,
                    targetWeight = profile.targetWeightKg,
                    currentWeight = latestWeight,
                    metabolismType = profile.metabolismType,
                    age = age,
                    activityLevel = profile.activityLevel,
                    dailyStepGoal = profile.dailyStepGoal
                )
            }
        }
    }

    fun logout() {
        tryAndLogScope {
            sessionManager.logout()
        }
    }

    fun updateProfileImage(imageBytes: ByteArray) {
        tryAndLogScope {
            getUserId { userId ->
                val newUrl = profileRepository.uploadAvatar(
                    userId,
                    imageBytes
                )

                val cacheBusterUrl = "$newUrl?t=${System.currentTimeMillis()}"
                internalUiState.update { it.copy(avatarUrl = cacheBusterUrl) }
            }
        }
    }

    fun updatePersonalData(name: String, email: String, metabolismType: com.example.calorietracker.data.model.MetabolismType, age: Int, confirmationMessage: String) {
        tryAndLogScope {
            getUserId { id ->
                val profile = profileRepository.getProfile(id) ?: return@getUserId
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val birthYear = currentYear - age
                
                val updatedProfile = profile.copy(
                    displayName = name,
                    metabolismType = metabolismType,
                    birthYear = birthYear
                )
                
                profileRepository.upsertProfile(updatedProfile)
                
                if (email != internalUiState.value.email) {
                    profileRepository.updateEmail(email)
                }

                internalUiState.update {
                    it.copy(
                        displayName = name,
                        email = email,
                        metabolismType = metabolismType,
                        age = age,
                        successMessage = if (email != it.email) confirmationMessage else "Persönliche Daten gespeichert"
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun updateGoals(
        calorieGoal: Int?,
        proteinGoal: Int?,
        carbsGoal: Int?,
        fatGoal: Int?,
        proteinRatio: Int?,
        carbsRatio: Int?,
        fatRatio: Int?,
        waterGoalMl: Int,
        weightStrategy: WeightStrategy,
        weeklyGoal: Double,
        targetWeight: Double?,
        newCurrentWeight: Double?,
        activityLevel: com.example.calorietracker.data.model.ActivityLevel,
        dailyStepGoal: Int
    ) {
        tryAndLogScope {
            getUserId { id ->
                val profile = profileRepository.getProfile(id)
                if (profile == null) {
                    internalUiState.update { it.copy(error = "Failed to load profile") }
                    return@getUserId
                }

                val updatedProfile = profile.copy(
                    calorieGoal = calorieGoal,
                    proteinGoal = proteinGoal,
                    carbsGoal = carbsGoal,
                    fatGoal = fatGoal,
                    proteinRatio = proteinRatio,
                    carbsRatio = carbsRatio,
                    fatRatio = fatRatio,
                    waterGoalMl = waterGoalMl,
                    weightStrategy = weightStrategy,
                    weeklyGoal = weeklyGoal,
                    targetWeightKg = targetWeight,
                    activityLevel = activityLevel,
                    dailyStepGoal = dailyStepGoal
                )
                profileRepository.upsertProfile(updatedProfile)
                
                // Add weight log if weight changed
                if (newCurrentWeight != null && newCurrentWeight != internalUiState.value.currentWeight) {
                    weightRepository.addWeightLog(
                        com.example.calorietracker.data.model.AddWeightLogDTO(
                            userId = id,
                            weightKg = newCurrentWeight,
                            loggedAt = Clock.System.now()
                        )
                    )
                }

                internalUiState.update {
                    it.copy(
                        calorieGoal = calorieGoal,
                        proteinGoal = proteinGoal,
                        carbsGoal = carbsGoal,
                        fatGoal = fatGoal,
                        proteinRatio = proteinRatio,
                        carbsRatio = carbsRatio,
                        fatRatio = fatRatio,
                        waterGoalMl = waterGoalMl,
                        weightStrategy = weightStrategy,
                        weeklyGoal = weeklyGoal,
                        targetWeight = targetWeight,
                        currentWeight = newCurrentWeight ?: it.currentWeight,
                        activityLevel = activityLevel,
                        dailyStepGoal = dailyStepGoal,
                        successMessage = "Ziele erfolgreich aktualisiert"
                    )
                }
            }
        }
    }

    fun recalculateCalories(
        strategy: WeightStrategy,
        weeklyGoal: Double,
        activityLevel: com.example.calorietracker.data.model.ActivityLevel? = null
    ) {
        val currentState = internalUiState.value
        val weight = currentState.currentWeight
        val finalActivity = activityLevel ?: currentState.activityLevel
        
        tryAndLogScope {
            getUserId { id ->
                val profile = profileRepository.getProfile(id) ?: return@getUserId
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val age = profile.birthYear?.let { currentYear - it } ?: 25

                if (weight != null && profile.heightCm != null && finalActivity != null) {
                    val result = CalorieCalculator.calculate(
                        metabolismType = profile.metabolismType,
                        age = age,
                        heightCm = profile.heightCm,
                        weightKg = weight,
                        activityLevel = finalActivity,
                        weightStrategy = strategy,
                        weeklyGoalKg = weeklyGoal
                    )
                    
                    // If we have custom ratios, use them for the calculated display
                    val currentCals = currentState.calorieGoal ?: result.calories
                    val finalProtein = if (currentState.proteinRatio != null) 
                        (currentCals * (currentState.proteinRatio / 100.0) / 4).toInt() else result.protein
                    val finalCarbs = if (currentState.carbsRatio != null) 
                        (currentCals * (currentState.carbsRatio / 100.0) / 4).toInt() else result.carbs
                    val finalFat = if (currentState.fatRatio != null) 
                        (currentCals * (currentState.fatRatio / 100.0) / 9).toInt() else result.fat

                    internalUiState.update { 
                        it.copy(
                            calculatedCalories = result.calories,
                            calculatedProtein = finalProtein,
                            calculatedCarbs = finalCarbs,
                            calculatedFat = finalFat
                        ) 
                    }
                }
            }
        }
    }

    fun clearMessages() {
        internalUiState.update { it.copy(error = null, successMessage = null) }
    }
}
