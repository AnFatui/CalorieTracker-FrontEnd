package com.example.calorietracker.ui.viewmodel

import com.example.calorietracker.data.repository.HealthConnectRepository
import com.example.calorietracker.data.repository.ProfileRepository
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class StepTrackingUiState(
    override val loading: Boolean = false,
    override val error: String? = null,
    val steps: Int = 0,
    val stepGoal: Int? = null,
    val isHealthConnectAvailable: Boolean = false,
    val hasStepsPermission: Boolean = false,
) : UiState<StepTrackingUiState> {
    override fun copyFlags(loading: Boolean, error: String?): StepTrackingUiState {
        return this.copy(loading = loading, error = error)
    }
}

class StepTrackingViewModel(
    override val sessionManager: SessionManager,
    private val profileRepository: ProfileRepository,
    private val healthConnectRepository: HealthConnectRepository,
) : BaseViewModel<StepTrackingUiState>() {
    override val tag: String = "StepTrackingViewModel"
    override val internalUiState = MutableStateFlow(StepTrackingUiState())
    val uiState: StateFlow<StepTrackingUiState> = internalUiState.asStateFlow()

    val stepsPermissions = HealthConnectRepository.STEP_PERMISSIONS

    init {
        loadData()
    }

    fun refresh() {
        loadData()
    }

    private fun loadData() {
        tryAndLogScope {
            getUserId { userId ->
                val profile = profileRepository.getProfile(userId)
                val available = healthConnectRepository.isAvailable()
                val hasPermission = if (available) healthConnectRepository.hasStepsPermission() else false
                val steps = healthConnectRepository.getTodaySteps()

                internalUiState.update {
                    it.copy(
                        steps = steps,
                        stepGoal = profile?.dailyStepGoal,
                        isHealthConnectAvailable = available,
                        hasStepsPermission = hasPermission
                    )
                }
            }
        }
    }

    fun saveStepGoal(goal: Int) {
        tryAndLogScope {
            getUserId { userId ->
                val profile = profileRepository.getProfile(userId) ?: return@getUserId
                profileRepository.upsertProfile(profile.copy(dailyStepGoal = goal))
                internalUiState.update { it.copy(stepGoal = goal) }
            }
        }
    }
}
