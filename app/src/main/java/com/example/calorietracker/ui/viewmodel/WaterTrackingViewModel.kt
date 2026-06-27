package com.example.calorietracker.ui.viewmodel

import com.example.calorietracker.data.model.WaterLog
import com.example.calorietracker.data.repository.WaterRepository
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class WaterTrackingUiState(
    override val error: String? = null,
    override val loading: Boolean = false,
    val todayLogs: List<WaterLog> = emptyList(),
    val totalAmountMl: Int = 0,
    val waterGoalMl: Int = 2000,
) : UiState<WaterTrackingUiState> {
    override fun copyFlags(
        loading: Boolean,
        error: String?
    ): WaterTrackingUiState {
        return this.copy(error = error, loading = loading)
    }
}

class WaterTrackingViewModel(
    override val sessionManager: SessionManager,
    val waterLogRepository: WaterRepository,
) : BaseViewModel<WaterTrackingUiState>() {
    override val tag: String = "WaterTrackingViewModel"
    override val internalUiState = MutableStateFlow(WaterTrackingUiState())
    val uiState: StateFlow<WaterTrackingUiState> = internalUiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        tryAndLogScope {
            getUserId { userId ->
                val logs = waterLogRepository.getPresentWaterLogs(userId)
                val total = logs.sumOf { it.amountMl }

                internalUiState.update {
                    it.copy(
                        todayLogs = logs,
                        totalAmountMl = total
                    )
                }
            }
        }
    }

    fun addWater(amountMl: Int) {
        tryAndLogScope {
            getUserId { userId ->
                val newLog = WaterLog(userId = userId, amountMl = amountMl)
                waterLogRepository.addWaterLog(newLog)
                loadData() // update
            }
        }
    }

    fun deleteLog(logId: String) {
        tryAndLogScope {
            waterLogRepository.deleteWaterLog(logId)
            loadData()
        }
    }

    fun deleteAllLogsForToday() {
        tryAndLogScope {
            getUserId { userId ->
                waterLogRepository.deleteAllWaterLogsForToday(userId)
                loadData()
            }
        }
    }
}
