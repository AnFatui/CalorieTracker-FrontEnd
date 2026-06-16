package com.example.calorietracker.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calorietracker.data.model.Profile
import com.example.calorietracker.data.model.WeightLog
import com.example.calorietracker.data.repository.ProfileRepository
import com.example.calorietracker.data.repository.WeightRepository
import com.example.calorietracker.providers.SupabaseClientProvider
import com.example.calorietracker.util.CalorieCalculator
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val selectedGoal: String = "",
    val weeklyGoal: Double = 0.5,
    val currentWeight: String = "",
    val targetWeight: String = "",
    val heightCm: String = "175",
    val age: String = "25",
    val sex: String = "Männlich",
    val activityLevel: String = "Moderat aktiv",
    val isLoading: Boolean = false,
    val error: String? = null
)

class OnboardingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val profileRepository = ProfileRepository()
    private val weightRepository = WeightRepository()
    private val TAG = "OnboardingViewModel"

    fun updateGoal(goal: String) {
        _uiState.update { it.copy(selectedGoal = goal) }
    }

    fun updateWeeklyGoal(value: Double) {
        _uiState.update { it.copy(weeklyGoal = value) }
    }

    fun updateCurrentWeight(weight: String) {
        _uiState.update { it.copy(currentWeight = weight) }
    }

    fun updateTargetWeight(weight: String) {
        _uiState.update { it.copy(targetWeight = weight) }
    }

    fun completeOnboarding(onSuccess: () -> Unit) {
        val state = _uiState.value
        val userId = SupabaseClientProvider.supabase.auth.currentUserOrNull()?.id
        if(userId == null) {
            Log.e(TAG, "Failed to complete onboarding process, current user is null")
            _uiState.update { it.copy(error = "Failed to complete onboarding process, please try again later") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val weight = state.currentWeight.toDoubleOrNull() ?: 0.0
                val targetWeight = state.targetWeight.toDoubleOrNull() ?: weight
                val height = state.heightCm.toIntOrNull() ?: 175
                val age = state.age.toIntOrNull() ?: 25

                // 1. Kalorien berechnen
                val calcResult = CalorieCalculator.calculate(
                    gender = state.sex,
                    age = age,
                    heightCm = height,
                    weightKg = weight,
                    activityLevel = state.activityLevel,
                    goal = state.selectedGoal,
                    weeklyGoalKg = state.weeklyGoal
                )

                // 2. Profil erstellen/updaten
                val updatedProfile = Profile(
                    id = userId,
                    selectedGoal = state.selectedGoal,
                    weeklyGoal = state.weeklyGoal,
                    targetWeightKg = targetWeight,
                    heightCm = height,
                    calorieGoal = calcResult.calories,
                    proteinGoal = calcResult.protein,
                    carbsGoal = calcResult.carbs,
                    fatGoal = calcResult.fat
                )
                profileRepository.upsertProfile(updatedProfile)

                // 3. Erstes Gewicht loggen
                weightRepository.addWeightLog(
                    WeightLog(userId = userId, weightKg = weight)
                )

                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Onboarding Fehler", e)
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
