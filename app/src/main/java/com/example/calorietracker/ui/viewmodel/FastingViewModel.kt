package com.example.calorietracker.ui.viewmodel

import com.example.calorietracker.data.model.FastingSchedule
import com.example.calorietracker.data.repository.FastingScheduleRepository
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class FastingPreset(
    val name: String,
    val duration: Int,
    val description: String
)

data class FastingUiState(
    override val loading: Boolean = false,
    override val error: String? = null,
    val schedule: FastingSchedule? = null,
    val isEditing: Boolean = false,
    val selectedDuration: Int = 16,
    val selectedStartTime: LocalTime = LocalTime(20, 0),
    val isFasting: Boolean = false,
    val showPresets: Boolean = false,
    val presets: List<FastingPreset> = listOf(
        FastingPreset("16:8", 16, "Der Klassiker: 16h Fasten, 8h Essen"),
        FastingPreset("18:6", 18, "Fortgeschritten: 18h Fasten, 6h Essen"),
        FastingPreset("20:4", 20, "Warrior Diet: 20h Fasten, 4h Essen"),
        FastingPreset("14:10", 14, "Einsteiger: 14h Fasten, 10h Essen")
    )
) : UiState<FastingUiState> {
    override fun copyFlags(loading: Boolean, error: String?): FastingUiState {
        return this.copy(loading = loading, error = error)
    }
}

class FastingViewModel(
    private val repository: FastingScheduleRepository,
    override val sessionManager: SessionManager
) : BaseViewModel<FastingUiState>() {
    override val internalUiState = MutableStateFlow(FastingUiState())
    val uiState = internalUiState.asStateFlow()
    override val tag: String = "FastingViewModel"

    init {
        loadFastingSchedule()
    }

    private fun loadFastingSchedule() {
        tryAndLogScope {
            getUserId { userId ->
                val schedule = repository.getFastingSchedule(userId)
                internalUiState.update { 
                    it.copy(
                        schedule = schedule,
                        selectedDuration = schedule?.durationHours ?: 16,
                        selectedStartTime = schedule?.startTime ?: LocalTime(20, 0),
                        isFasting = schedule?.let { s -> checkIfFasting(s) } ?: false,
                        showPresets = schedule == null // Show presets by default if no schedule
                    ) 
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun getCurrentLocalTime(): LocalTime {
        val now = Clock.System.now()
        return now.toLocalDateTime(TimeZone.currentSystemDefault()).time
    }

    private fun checkIfFasting(schedule: FastingSchedule): Boolean {
        if (!schedule.isActive) return false
        
        val now = getCurrentLocalTime()
        val start = schedule.startTime
        val duration = schedule.durationHours
        
        val startInMinutes = start.hour * 60 + start.minute
        val nowInMinutes = now.hour * 60 + now.minute
        val durationInMinutes = duration * 60
        val endInMinutes = startInMinutes + durationInMinutes

        return if (endInMinutes <= 1440) {
            nowInMinutes >= startInMinutes && nowInMinutes < endInMinutes
        } else {
            val rolloverEnd = endInMinutes % 1440
            nowInMinutes >= startInMinutes || nowInMinutes < rolloverEnd
        }
    }

    fun toggleEdit() {
        internalUiState.update { it.copy(isEditing = !it.isEditing, showPresets = false) }
    }

    fun togglePresets() {
        internalUiState.update { it.copy(showPresets = !it.showPresets, isEditing = false) }
    }

    fun cancelEdit() {
        internalUiState.update { 
            it.copy(
                isEditing = false,
                selectedDuration = it.schedule?.durationHours ?: 16,
                selectedStartTime = it.schedule?.startTime ?: LocalTime(20, 0),
                showPresets = it.schedule == null
            ) 
        }
    }

    fun selectPreset(preset: FastingPreset) {
        internalUiState.update { 
            it.copy(
                selectedDuration = preset.duration,
                isEditing = true,
                showPresets = false
            ) 
        }
    }

    fun updateDuration(hours: Int) {
        internalUiState.update { it.copy(selectedDuration = hours) }
    }

    fun selectStartTime(time: LocalTime) {
        internalUiState.update { it.copy(selectedStartTime = time) }
    }

    fun updateStartTime(time: LocalTime) {
        internalUiState.update { it.copy(selectedStartTime = time) }
    }

    fun saveSchedule() {
        tryAndLogScope {
            getUserId { userId ->
                val currentSchedule = internalUiState.value.schedule
                val newSchedule = FastingSchedule(
                    id = currentSchedule?.id ?: UUID.randomUUID().toString(),
                    userId = userId,
                    durationHours = internalUiState.value.selectedDuration,
                    startTime = internalUiState.value.selectedStartTime,
                    isActive = true
                )
                repository.upsertFastingSchedule(newSchedule)
                internalUiState.update { 
                    it.copy(
                        schedule = newSchedule, 
                        isFasting = checkIfFasting(newSchedule),
                        isEditing = false,
                        showPresets = false
                    ) 
                }
            }
        }
    }
}
