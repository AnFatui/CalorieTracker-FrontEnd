package com.example.calorietracker.ui.viewmodel

import com.example.calorietracker.data.model.MealLog
import com.example.calorietracker.data.model.WaterLog
import com.example.calorietracker.data.repository.HealthConnectRepository
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
import java.time.Instant
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

data class ChartValue(
    val label: String,
    val value: Int,
    val isCurrent: Boolean
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
    val weeklyCalories: List<ChartValue> = emptyList(),
    val weeklyWater: List<ChartValue> = emptyList(),
    val monthCalories: Int? = null,
    val monthCalorieGoal: Int? = null,
    val monthProteins: Int? = null,
    val monthProteinGoal: Int? = null,
    val monthCarbs: Int? = null,
    val monthCarbGoal: Int? = null,
    val monthFat: Int? = null,
    val monthFatGoal: Int? = null,
    val monthlyCalories: List<ChartValue> = emptyList(),
    val monthlyWater: List<ChartValue> = emptyList(),
    val yearCalories: Int? = null,
    val yearCalorieGoal: Int? = null,
    val yearProteins: Int? = null,
    val yearProteinGoal: Int? = null,
    val yearCarbs: Int? = null,
    val yearCarbGoal: Int? = null,
    val yearFat: Int? = null,
    val yearFatGoal: Int? = null,
    val yearlyCalories: List<ChartValue> = emptyList(),
    val yearlyWater: List<ChartValue> = emptyList(),
    val weeklySteps: List<ChartValue> = emptyList(),
    val monthlySteps: List<ChartValue> = emptyList(),
    val yearlySteps: List<ChartValue> = emptyList(),
    val hasStepsPermission: Boolean = false
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
    private val mealRepository: MealRepository,
    private val healthConnectRepository: HealthConnectRepository
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
                ) { date -> weekMeals.filter { logDate(it.loggedAt) == date }.sumOf { it.calories } }

                val weekWaterLogs = waterRepository.getWaterLogsInRange(userId, monday, nextMonday)
                val weeklyWater = buildDailyChartValues(
                    monday, today
                ) { date -> weekWaterLogs.filter { logDate(it.loggedAt) == date }.sumOf { it.amountMl } }

                val firstOfMonth = today.withDayOfMonth(1)
                val firstOfNextMonth = firstOfMonth.plusMonths(1)
                val monthMeals = mealRepository.getMealLogsInRange(userId, firstOfMonth, firstOfNextMonth)
                val monthWaterLogs = waterRepository.getWaterLogsInRange(userId, firstOfMonth, firstOfNextMonth)
                val monthlyCalories = buildWeekBucketsForMonth(firstOfMonth, today) { start, end ->
                    caloriesInRange(monthMeals, start, end)
                }
                val monthlyWater = buildWeekBucketsForMonth(firstOfMonth, today) { start, end ->
                    waterInRange(monthWaterLogs, start, end)
                }
                val daysInMonth = YearMonth.from(today).lengthOfMonth()

                val firstOfYear = today.withDayOfYear(1)
                val firstOfNextYear = firstOfYear.plusYears(1)
                val yearMeals = mealRepository.getMealLogsInRange(userId, firstOfYear, firstOfNextYear)
                val yearWaterLogs = waterRepository.getWaterLogsInRange(userId, firstOfYear, firstOfNextYear)
                val yearlyCalories = buildMonthBucketsForYear(firstOfYear, today) { start, end ->
                    caloriesInRange(yearMeals, start, end)
                }
                val yearlyWater = buildMonthBucketsForYear(firstOfYear, today) { start, end ->
                    waterInRange(yearWaterLogs, start, end)
                }
                val daysInYear = if (Year.isLeap(today.year.toLong())) 366 else 365

                val hasStepsPermission = healthConnectRepository.isAvailable() &&
                    healthConnectRepository.hasStepsPermission()
                val weekSteps = healthConnectRepository.getStepsInRange(monday, nextMonday)
                val weeklySteps = buildDailyChartValues(
                    monday, today
                ) { date -> weekSteps[date] ?: 0 }
                val monthSteps = healthConnectRepository.getStepsInRange(firstOfMonth, firstOfNextMonth)
                val monthlySteps = buildWeekBucketsForMonth(firstOfMonth, today) { start, end ->
                    stepsInRange(monthSteps, start, end)
                }
                val yearSteps = healthConnectRepository.getStepsInRange(firstOfYear, firstOfNextYear)
                val yearlySteps = buildMonthBucketsForYear(firstOfYear, today) { start, end ->
                    stepsInRange(yearSteps, start, end)
                }

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
                        weeklyWater = weeklyWater,
                        monthCalories = monthMeals.sumOf { meal -> meal.calories },
                        monthCalorieGoal = dailyCalorieGoal?.times(daysInMonth),
                        monthProteins = monthMeals.sumOf { meal -> meal.protein },
                        monthProteinGoal = dailyProteinGoal?.times(daysInMonth),
                        monthCarbs = monthMeals.sumOf { meal -> meal.carbs },
                        monthCarbGoal = dailyCarbGoal?.times(daysInMonth),
                        monthFat = monthMeals.sumOf { meal -> meal.fat },
                        monthFatGoal = dailyFatGoal?.times(daysInMonth),
                        monthlyCalories = monthlyCalories,
                        monthlyWater = monthlyWater,
                        yearCalories = yearMeals.sumOf { meal -> meal.calories },
                        yearCalorieGoal = dailyCalorieGoal?.times(daysInYear),
                        yearProteins = yearMeals.sumOf { meal -> meal.protein },
                        yearProteinGoal = dailyProteinGoal?.times(daysInYear),
                        yearCarbs = yearMeals.sumOf { meal -> meal.carbs },
                        yearCarbGoal = dailyCarbGoal?.times(daysInYear),
                        yearFat = yearMeals.sumOf { meal -> meal.fat },
                        yearFatGoal = dailyFatGoal?.times(daysInYear),
                        yearlyCalories = yearlyCalories,
                        yearlyWater = yearlyWater,
                        weeklySteps = weeklySteps,
                        monthlySteps = monthlySteps,
                        yearlySteps = yearlySteps,
                        hasStepsPermission = hasStepsPermission
                    )
                }
            }
        }
    }

    private fun buildDailyChartValues(
        monday: LocalDate,
        today: LocalDate,
        valueForDate: (LocalDate) -> Int
    ): List<ChartValue> {
        val dayLabels = listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")
        return (0..6).map { offset ->
            val date = monday.plusDays(offset.toLong())
            ChartValue(
                label = dayLabels[offset],
                value = valueForDate(date),
                isCurrent = date == today
            )
        }
    }

    private fun buildWeekBucketsForMonth(
        firstOfMonth: LocalDate,
        today: LocalDate,
        valueForRange: (LocalDate, LocalDate) -> Int
    ): List<ChartValue> {
        val firstOfNextMonth = firstOfMonth.plusMonths(1)
        val buckets = mutableListOf<ChartValue>()
        var bucketStart = firstOfMonth
        var index = 1
        while (bucketStart < firstOfNextMonth) {
            val bucketEnd = if (bucketStart.plusDays(7) < firstOfNextMonth) {
                bucketStart.plusDays(7)
            } else {
                firstOfNextMonth
            }
            buckets.add(
                ChartValue(
                    label = "W$index",
                    value = valueForRange(bucketStart, bucketEnd),
                    isCurrent = !today.isBefore(bucketStart) && today.isBefore(bucketEnd)
                )
            )
            bucketStart = bucketEnd
            index++
        }
        return buckets
    }

    private fun buildMonthBucketsForYear(
        firstOfYear: LocalDate,
        today: LocalDate,
        valueForRange: (LocalDate, LocalDate) -> Int
    ): List<ChartValue> {
        val monthLabels = listOf("Jan", "Feb", "Mär", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez")
        return (0..11).map { offset ->
            val monthStart = firstOfYear.plusMonths(offset.toLong())
            val monthEnd = monthStart.plusMonths(1)
            ChartValue(
                label = monthLabels[offset],
                value = valueForRange(monthStart, monthEnd),
                isCurrent = monthStart.year == today.year && monthStart.month == today.month
            )
        }
    }

    private fun logDate(loggedAt: String?): LocalDate? =
        loggedAt?.let { runCatching { Instant.parse(it).atZone(ZoneId.systemDefault()).toLocalDate() }.getOrNull() }

    private fun caloriesInRange(logs: List<MealLog>, start: LocalDate, endExclusive: LocalDate): Int =
        logs.filter { log ->
            val date = logDate(log.loggedAt)
            date != null && !date.isBefore(start) && date.isBefore(endExclusive)
        }.sumOf { it.calories }

    private fun waterInRange(logs: List<WaterLog>, start: LocalDate, endExclusive: LocalDate): Int =
        logs.filter { log ->
            val date = logDate(log.loggedAt)
            date != null && !date.isBefore(start) && date.isBefore(endExclusive)
        }.sumOf { it.amountMl }

    private fun stepsInRange(steps: Map<LocalDate, Int>, start: LocalDate, endExclusive: LocalDate): Int =
        steps.filterKeys { date -> !date.isBefore(start) && date.isBefore(endExclusive) }
            .values.sum()
}
