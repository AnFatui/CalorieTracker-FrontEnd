package com.example.calorietracker.ui.viewmodel

import android.util.Log
import com.example.calorietracker.data.model.FastingSchedule
import com.example.calorietracker.data.repository.FastingScheduleRepository
import com.example.calorietracker.data.repository.ProfileRepository
import com.example.calorietracker.data.repository.WaterRepository
import com.example.calorietracker.data.repository.WeightRepository
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

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
    val stepGoal: Int? = null,
    val fastingProgress: Float? = null,
    val isFasting: Boolean = false,
    val fastingRemainingText: String? = null,
    val fastingTypeText: String? = null,
    val fastingEndText: String? = null
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
    private val fastingRepository: FastingScheduleRepository,
    override val sessionManager: SessionManager,
) : BaseViewModel<HomeUiState>() {
    override val tag: String = "HomeViewModel"
    override val internalUiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = internalUiState.asStateFlow()

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
                if (profile == null) {
                    internalUiState.update { it.copy(error = "Failed to load profile") }
                    Log.e(tag, "Failed to load with id $userId, profile, null")
                    return@getUserId
                }

                val currentWeightLog = weightRepository.getLatestWeightLog(userId)
                val fastingSchedule = fastingRepository.getFastingSchedule(userId)

                val presentWaterMl = getPresentWaterLevel(userId)

                internalUiState.update {
                    val now = getCurrentLocalTime()
                    val isFastingActive = fastingSchedule?.let { s -> isCurrentlyFasting(s, now) } ?: false
                    it.copy(
                        displayName =  profile.displayName,
                        currentWeight = currentWeightLog?.weightKg,
                        targetWeight = profile.targetWeightKg,
                        calorieGoal = profile.calorieGoal,
                        waterMl = presentWaterMl,
                        waterGoalMl = profile.waterGoalMl,
                        stepGoal = profile.dailyStepGoal,
                        fastingProgress = fastingSchedule?.let { s -> calculateCurrentProgress(s, isFastingActive, now) },
                        isFasting = isFastingActive,
                        fastingRemainingText = fastingSchedule?.let { s -> getFastingText(s, now) },
                        fastingTypeText = fastingSchedule?.let { s -> "${s.durationHours}:${24 - s.durationHours}" },
                        fastingEndText = fastingSchedule?.let { s -> calculateFastingStatusTime(s, isFastingActive) }
                    )
                }
            }
        }
    }

    private fun calculateFastingStatusTime(schedule: FastingSchedule, isFasting: Boolean): String {
        val startHour = schedule.startTime.hour
        val startMinute = schedule.startTime.minute
        
        return if (isFasting) {
            val endHour = (startHour + schedule.durationHours) % 24
            String.format("%02d:%02d Uhr", endHour, startMinute)
        } else {
            String.format("%02d:%02d Uhr", startHour, startMinute)
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun getCurrentLocalTime(): LocalTime {
        val now = Clock.System.now()
        return now.toLocalDateTime(TimeZone.currentSystemDefault()).time
    }

    private fun isCurrentlyFasting(schedule: FastingSchedule, now: LocalTime): Boolean {
        if (!schedule.isActive) return false
        
        val start = schedule.startTime
        val duration = schedule.durationHours
        
        val startInMinutes = start.hour * 60 + start.minute
        val nowInMinutes = now.hour * 60 + now.minute
        val durationInMinutes = duration * 60
        
        return isTimeInWindow(nowInMinutes, startInMinutes, durationInMinutes)
    }
    
    private fun isTimeInWindow(nowMinutes: Int, startMinutes: Int, durationMinutes: Int): Boolean {
        val endMinutes = startMinutes + durationMinutes
        return if (endMinutes <= 1440) {
            nowMinutes >= startMinutes && nowMinutes < endMinutes
        } else {
            val rolloverEnd = endMinutes % 1440
            nowMinutes >= startMinutes || nowMinutes < rolloverEnd
        }
    }

    private fun calculateCurrentProgress(schedule: FastingSchedule, isFasting: Boolean, now: LocalTime): Float {
        if (!schedule.isActive) return 0f
        
        val nowMin = now.hour * 60 + now.minute
        val fastingStartMin = schedule.startTime.hour * 60 + schedule.startTime.minute
        val fastingDurationMin = schedule.durationHours * 60
        
        return if (isFasting) {
            val passedMin = if (nowMin >= fastingStartMin) nowMin - fastingStartMin else (1440 - fastingStartMin) + nowMin
            (passedMin.toFloat() / fastingDurationMin.toFloat()).coerceIn(0f, 1f)
        } else {
            val eatingStartMin = (fastingStartMin + fastingDurationMin) % 1440
            val eatingDurationMin = 1440 - fastingDurationMin
            val passedMin = if (nowMin >= eatingStartMin) nowMin - eatingStartMin else (1440 - eatingStartMin) + nowMin
            (passedMin.toFloat() / eatingDurationMin.toFloat()).coerceIn(0f, 1f)
        }
    }

    private fun getFastingText(schedule: FastingSchedule, now: LocalTime): String {
        if (!schedule.isActive) return ""
        val startMin = schedule.startTime.hour * 60 + schedule.startTime.minute
        val durationMin = schedule.durationHours * 60
        val nowMin = now.hour * 60 + now.minute
        
        return if (isTimeInWindow(nowMin, startMin, durationMin)) {
            val passedMin = if (nowMin >= startMin) nowMin - startMin else (1440 - startMin) + nowMin
            val remainingMin = durationMin - passedMin
            val h = remainingMin / 60
            val m = remainingMin % 60
            "Fasten: ${h}h ${m}m verbleibend"
        } else {
            val waitMin = if (startMin > nowMin) startMin - nowMin else (1440 - nowMin) + startMin
            val h = waitMin / 60
            val m = waitMin % 60
            "Startet in ${h}h ${m}m"
        }
    }

    private suspend fun getPresentWaterLevel(userId: String): Int {
        val presentWaterLogs = waterRepository.getPresentWaterLogs(userId)
        return presentWaterLogs.sumOf { it.amountMl }
    }
}