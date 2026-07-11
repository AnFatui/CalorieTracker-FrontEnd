package com.example.calorietracker.ui.viewmodel

import android.util.Log
import com.example.calorietracker.data.model.FastingSchedule
import com.example.calorietracker.data.repository.FastingScheduleRepository
import com.example.calorietracker.data.repository.HealthConnectRepository
import com.example.calorietracker.data.repository.MealRepository
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
import java.util.Locale
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
    private val healthConnectRepository: HealthConnectRepository,
    private val mealRepository: MealRepository,
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
                val todaySteps = healthConnectRepository.getTodaySteps()
                val todayMeals = mealRepository.getTodayMealLogs(userId)

                // Calculate fallback calories if no manual goal is set
                val calcResult = if (profile.calorieGoal == null) {
                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    val age = profile.birthYear?.let { currentYear - it } ?: 25
                    val latestWeight = currentWeightLog?.weightKg

                    if (latestWeight != null && profile.heightCm != null && profile.activityLevel != null && profile.weightStrategy != null) {
                        com.example.calorietracker.util.CalorieCalculator.calculate(
                            metabolismType = profile.metabolismType,
                            age = age,
                            heightCm = profile.heightCm,
                            weightKg = latestWeight,
                            activityLevel = profile.activityLevel,
                            weightStrategy = profile.weightStrategy,
                            weeklyGoalKg = profile.weeklyGoal ?: 0.5
                        )
                    } else null
                } else null

                val dailyCalorieGoal = profile.calorieGoal ?: calcResult?.calories
                // Macro goals aren't stored as grams - only the chosen ratio is persisted,
                // grams are derived from the calorie goal on the fly (see ProfileScreen's GoalsPage).
                val dailyMacroGoals = dailyCalorieGoal?.let {
                    com.example.calorietracker.util.CalorieCalculator.macroGoalsFromRatios(
                        it,
                        profile.proteinRatio,
                        profile.carbsRatio,
                        profile.fatRatio
                    )
                }

                internalUiState.update {
                    val now = getCurrentLocalTime()
                    val isFastingActive = fastingSchedule?.let { s -> isCurrentlyFasting(s, now) } ?: false
                    it.copy(
                        displayName =  profile.displayName,
                        currentWeight = currentWeightLog?.weightKg,
                        targetWeight = profile.targetWeightKg,
                        calories = todayMeals.sumOf { meal -> meal.calories },
                        calorieGoal = dailyCalorieGoal,
                        protein = todayMeals.sumOf { meal -> meal.protein },
                        proteinGoal = dailyMacroGoals?.protein,
                        carbs = todayMeals.sumOf { meal -> meal.carbs },
                        carbsGoal = dailyMacroGoals?.carbs,
                        fat = todayMeals.sumOf { meal -> meal.fat },
                        fatGoal = dailyMacroGoals?.fat,
                        waterMl = presentWaterMl,
                        waterGoalMl = profile.waterGoalMl,
                        steps = todaySteps,
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
            String.format(Locale.getDefault(), "%02d:%02d Uhr", endHour, startMinute)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d Uhr", startHour, startMinute)
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