package com.example.calorietracker.ui.viewmodel


import androidx.lifecycle.viewModelScope
import com.example.calorietracker.data.model.AddWeightLogDTO
import com.example.calorietracker.data.model.WeightLog
import com.example.calorietracker.data.repository.ProfileRepository
import com.example.calorietracker.data.repository.WeightRepository
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class WeightTrackingUiState(
    override val loading: Boolean = false,
    override val error: String? = null,
    val currentWeightKg: Double? = null,
    val targetWeightKg: Double? = null,
    val weightLogs: List<WeightLog> = listOf()
) : UiState<WeightTrackingUiState> {
    override fun copyFlags(
        loading: Boolean,
        error: String?
    ): WeightTrackingUiState {
        return this.copy(loading = loading, error = error)
    }
}

class WeightTrackingViewModel(
    private val weightRepository: WeightRepository,
    override val sessionManager: SessionManager,
    private val profileRepository: ProfileRepository
) : BaseViewModel<WeightTrackingUiState>() {
    override val tag: String = "WeightTrackingViewModel"
    override val internalUiState = MutableStateFlow(WeightTrackingUiState())
    val uiState = internalUiState.asStateFlow()

    init {
        loadData()
    }

    @OptIn(ExperimentalTime::class)
    private fun loadData() {
        tryAndLogScope {
            // Get data
            val userId = sessionManager.currentUserId ?: return@tryAndLogScope
            val profile = profileRepository.getProfile(userId) ?: return@tryAndLogScope
            val logs = weightRepository.getWeightLogs(userId)

            // Assign it
            internalUiState.update {
                it.copy(
                    weightLogs = logs,
                    currentWeightKg = logs.maxByOrNull { l -> l.loggedAt }?.weightKg ?: profile.targetWeightKg ?: 70.0,
                    targetWeightKg = profile.targetWeightKg
                )
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun adjustWeight(delta: Double) {
        val currentWeight = internalUiState.value.currentWeightKg ?: return
        val newWeight = currentWeight + delta
        updateWeight(newWeight)
    }

    @OptIn(ExperimentalTime::class)
    fun updateWeight(weightKg: Double) {
        // Optimistic update
        internalUiState.update { it.copy(currentWeightKg = weightKg) }

        viewModelScope.launch {
            tryAndLogScope {
                getUserId { userId ->
                    val latestLog = weightRepository.getLatestWeightLog(userId)
                    val now = Clock.System.now()
                    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
                    
                    val isToday = latestLog != null && 
                            latestLog.loggedAt.toLocalDateTime(TimeZone.currentSystemDefault()).date == today

                    if (isToday && latestLog.id != null) {
                        weightRepository.updateWeightLog(latestLog.id, weightKg)
                    } else {
                        weightRepository.addWeightLog(
                            AddWeightLogDTO(
                                userId = userId,
                                weightKg = weightKg,
                                loggedAt = now
                            )
                        )
                    }
                    // Refresh logs to update history
                    val logs = weightRepository.getWeightLogs(userId)
                    internalUiState.update { it.copy(weightLogs = logs) }
                }
            }
        }
    }
}
