package com.example.calorietracker.ui.viewmodel

import com.example.calorietracker.data.model.AddWeightLogDTO
import com.example.calorietracker.data.model.WeightLog
import com.example.calorietracker.data.repository.ProfileRepository
import com.example.calorietracker.data.repository.WeightRepository
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

enum class WeightTimeRange {
    WEEK, YEAR
}

data class WeightTrackingUiState(
    override val loading: Boolean = false,
    override val error: String? = null,
    val currentWeightKg: Double? = null,
    val targetWeightKg: Double? = null,
    val weightLogs: List<WeightLog> = listOf(),
    val selectedTimeRange: WeightTimeRange = WeightTimeRange.WEEK
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
            getUserId { userId ->
                val profile = profileRepository.getProfile(userId)
                val logs = weightRepository.getWeightLogs(userId).sortedByDescending { it.loggedAt }

                internalUiState.update {
                    it.copy(
                        weightLogs = logs,
                        currentWeightKg = logs.firstOrNull()?.weightKg ?: profile?.targetWeightKg
                        ?: 70.0,
                        targetWeightKg = profile?.targetWeightKg
                    )
                }
            }
        }
    }

    fun setTimeRange(range: WeightTimeRange) {
        internalUiState.update { it.copy(selectedTimeRange = range) }
    }

    fun adjustWeight(delta: Double) {
        val currentWeight = internalUiState.value.currentWeightKg ?: return
        val newWeight = ((currentWeight + delta) * 10.0).roundToInt() / 10.0
        updateWeight(newWeight)
    }

    @OptIn(ExperimentalTime::class)
    fun updateWeight(weightKg: Double) {
        // Optimistisches UI Update
        internalUiState.update { it.copy(currentWeightKg = weightKg) }

        tryAndLogScope {
            getUserId { userId ->
                val now = Clock.System.now()
                val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date

                // Wir nutzen die Logs aus dem aktuellen State
                val currentLogs = internalUiState.value.weightLogs
                val todayLog = currentLogs
                    .filter { it.loggedAt.toLocalDateTime(TimeZone.currentSystemDefault()).date == today }
                    .maxByOrNull { it.loggedAt }

                val updatedLog = if (todayLog != null && todayLog.id != null) {
                    weightRepository.updateWeightLog(todayLog.id, weightKg)
                } else {
                    weightRepository.addWeightLog(
                        AddWeightLogDTO(userId = userId, weightKg = weightKg, loggedAt = now)
                    )
                }

                // Lokal die Liste updaten, statt alles neu zu laden
                internalUiState.update { state ->
                    val newList = if (todayLog != null) {
                        state.weightLogs.map { if (it.id == updatedLog.id) updatedLog else it }
                    } else {
                        (listOf(updatedLog) + state.weightLogs).sortedByDescending { it.loggedAt }
                    }
                    state.copy(weightLogs = newList, currentWeightKg = weightKg)
                }
            }
        }
    }
}
