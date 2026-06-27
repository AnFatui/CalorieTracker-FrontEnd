package com.example.calorietracker.ui.viewmodel

import android.util.Log
import com.example.calorietracker.data.repository.ProfileRepository
import com.example.calorietracker.data.repository.WaterRepository
import com.example.calorietracker.data.repository.WeightRepository
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HomeUiState(
    override val loading: Boolean = false,
    override val error: String? = null,
    val displayName: String? = null,
    val currentWeight: Double? = null,
    val targetWeight: Double? = null,
    val calories: Int = 0,
    val calorieGoal: Int? = null,
    val protein: Int = 0,
    val proteinGoal: Int? = null,
    val carbs: Int = 0,
    val carbsGoal: Int? = null,
    val fat: Int = 0,
    val fatGoal: Int? = null,
    val waterMl: Int = 0,
    val waterGoalMl: Int? = null,
    val steps: Int = 0,
    val stepGoal: Int? = null
) : UiState<HomeUiState> {
    override fun copyFlags(
        loading: Boolean,
        error: String?
    ): HomeUiState {
        return this.copy(loading = loading, error = error)
    }
}

class HomeViewModel(
    private val waterRepository: WaterRepository,
    private val weightRepository: WeightRepository,
    private val profileRepository: ProfileRepository,
    override val sessionManager: SessionManager,
) : BaseViewModel<HomeUiState>() {
    override val tag: String = "HomeViewModel"
    override val internalUiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = internalUiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        tryAndLogScope {
            getUserId { userId ->
                val profile = profileRepository.getProfile(userId)
                if (profile == null) {
                    internalUiState.update { it.copy(error = "Failed to load profile") }
                    Log.e(tag, "Failed to load with id $userId, profile, null")
                    return@getUserId
                }

                val currentWeightLog = weightRepository.getLatestWeightLog(userId)

                //val nutrition = nutritionRepository.getTodayNutrition()
                val presentWaterMl = getPresentWaterLevel(userId)
                //val steps = stepsRepository.getTodaySteps()

                internalUiState.update {
                    it.copy(
                        displayName =  profile.displayName,
                        currentWeight = currentWeightLog?.weightKg,
                        targetWeight = profile.targetWeightKg,
                        //calories = nutrition.calories,
                        calorieGoal = profile.calorieGoal,
                        waterMl = presentWaterMl,
                        waterGoalMl = profile.waterGoalMl,
                        //steps = steps.count,
                        stepGoal = profile.dailyStepGoal
                    )
                }
            }
        }
    }

    private suspend fun getPresentWaterLevel(userId: String): Int {
        val presentWaterLogs = waterRepository.getPresentWaterLogs(userId)
        return presentWaterLogs.sumOf { it.amountMl }
    }
}
