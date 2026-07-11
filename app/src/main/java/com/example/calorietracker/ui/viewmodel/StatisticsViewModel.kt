package com.example.calorietracker.ui.viewmodel

import com.example.calorietracker.data.repository.MealRepository
import com.example.calorietracker.data.repository.ProfileRepository
import com.example.calorietracker.data.repository.WaterRepository
import com.example.calorietracker.data.repository.WeightRepository
import com.example.calorietracker.util.CalorieCalculator
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class DailyChartValue(
    val dayLabel: String,
    val value: Int,
    val isToday: Boolean
)

data class StatisticsUiState(
    override val loading: Boolean = false,
    override val error: String? = null,
    val calories: Int? = null,
    val calorieGoal: Int? = null,
    val proteins: Int? = null,
    val proteinGoal: Int? = null,
    val carbs: Int? = null,
    val carbGoal: Int? = null,
    val fat: Int? = null,
    val fatGoal: Int? = null,
    val weeklyCalories: List<DailyChartValue> = emptyList(),
    val weeklyWater: List<DailyChartValue> = emptyList()
) : UiState<StatisticsUiState> {
    override fun copyFlags(
        loading: Boolean,
        error: String?
    ): StatisticsUiState {
        return this.copy(loading = loading, error = error)
    }
}

class StatisticsViewModel(
    override val sessionManager: SessionManager,
    private val waterRepository: WaterRepository,
    private val profileRepository: ProfileRepository,
    private val weightRepository: WeightRepository,
    private val mealRepository: MealRepository
) : BaseViewModel<StatisticsUiState>() {
    override val tag: String = "StatisticsViewModel"

    override val internalUiState = MutableStateFlow(StatisticsUiState())
    val uiState = internalUiState.asStateFlow()

    init {
        loadStatistics()
    }

    fun refresh() {
        loadStatistics()
    }

    private fun loadStatistics() {
        tryAndLogScope {
            getUserId { userId ->
                val profile = profileRepository.getProfile(userId)

                val today = LocalDate.now()
                val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val nextMonday = monday.plusDays(7)

                val weekMeals = mealRepository.getMealLogsInRange(userId, monday, nextMonday)
                val weeklyCalories = buildDailyChartValues(
                    monday, today
                ) { date -> weekMeals.filter { it.loggedAt?.take(10) == date.toString() }.sumOf { it.calories } }

                val weekWaterLogs = waterRepository.getWaterLogsInRange(userId, monday, nextMonday)
                val weeklyWater = buildDailyChartValues(
                    monday, today
                ) { date -> weekWaterLogs.filter { it.loggedAt?.take(10) == date.toString() }.sumOf { it.amountMl } }

                val currentWeightLog = weightRepository.getLatestWeightLog(userId)
                val calcResult = if (profile != null && profile.calorieGoal == null) {
                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    val age = profile.birthYear?.let { currentYear - it } ?: 25
                    val latestWeight = currentWeightLog?.weightKg

                    if (latestWeight != null && profile.heightCm != null && profile.activityLevel != null && profile.weightStrategy != null) {
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
                } else null

                val dailyCalorieGoal = profile?.calorieGoal ?: calcResult?.calories
                // Macro goals aren't stored as grams - only the chosen ratio is persisted,
                // grams are derived from the calorie goal on the fly (see ProfileScreen's GoalsPage).
                val dailyMacroGoals = dailyCalorieGoal?.let {
                    CalorieCalculator.macroGoalsFromRatios(
                        it,
                        profile?.proteinRatio,
                        profile?.carbsRatio,
                        profile?.fatRatio
                    )
                }
                val dailyProteinGoal = dailyMacroGoals?.protein
                val dailyCarbGoal = dailyMacroGoals?.carbs
                val dailyFatGoal = dailyMacroGoals?.fat

                internalUiState.update {
                    it.copy(
                        calories = weekMeals.sumOf { meal -> meal.calories },
                        calorieGoal = dailyCalorieGoal?.times(7),
                        proteins = weekMeals.sumOf { meal -> meal.protein },
                        proteinGoal = dailyProteinGoal?.times(7),
                        carbs = weekMeals.sumOf { meal -> meal.carbs },
                        carbGoal = dailyCarbGoal?.times(7),
                        fat = weekMeals.sumOf { meal -> meal.fat },
                        fatGoal = dailyFatGoal?.times(7),
                        weeklyCalories = weeklyCalories,
                        weeklyWater = weeklyWater
                    )
                }
            }
        }
    }

    private fun buildDailyChartValues(
        monday: LocalDate,
        today: LocalDate,
        valueForDate: (LocalDate) -> Int
    ): List<DailyChartValue> {
        val dayLabels = listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")
        return (0..6).map { offset ->
            val date = monday.plusDays(offset.toLong())
            DailyChartValue(
                dayLabel = dayLabels[offset],
                value = valueForDate(date),
                isToday = date == today
            )
        }
    }
}
