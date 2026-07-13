package com.example.calorietracker.ui.viewmodel

import com.example.calorietracker.data.model.MealLog
import com.example.calorietracker.data.repository.MealRepository
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

data class MealTrackingUiState(
    override val loading: Boolean = false,
    override val error: String? = null,
    val meals: List<MealLog> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val weekDates: List<LocalDate> = emptyList()
) : UiState<MealTrackingUiState> {
    override fun copyFlags(
        loading: Boolean,
        error: String?
    ): MealTrackingUiState {
        return this.copy(loading = loading, error = error)
    }
}

class MealTrackingViewModel(
    override val sessionManager: SessionManager,
    private val mealRepository: MealRepository
) : BaseViewModel<MealTrackingUiState>() {
    override val internalUiState = MutableStateFlow(MealTrackingUiState())
    override val tag: String = "MealTrackingViewModel"

    val uiState = internalUiState.asStateFlow()

    private var weekMeals: List<MealLog> = emptyList()

    init {
        loadMeals()
    }

    fun refresh() {
        loadMeals()
    }

    fun selectDate(date: LocalDate) {
        internalUiState.update { it.copy(meals = mealsForDate(date), selectedDate = date) }
    }

    fun deleteMeal(logId: String) {
        tryAndLogScope {
            mealRepository.deleteMealLog(logId)
            weekMeals = weekMeals.filter { it.id != logId }
            internalUiState.update { it.copy(meals = mealsForDate(it.selectedDate)) }
        }
    }

    private fun loadMeals() {
        tryAndLogScope {
            getUserId { userId ->
                val today = LocalDate.now()
                val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val nextMonday = monday.plusDays(7)
                val weekDates = (0..6).map { offset -> monday.plusDays(offset.toLong()) }

                weekMeals = mealRepository.getMealLogsInRange(userId, monday, nextMonday)

                val selectedDate = internalUiState.value.selectedDate.takeIf { it in weekDates } ?: today
                internalUiState.update {
                    it.copy(
                        meals = mealsForDate(selectedDate),
                        selectedDate = selectedDate,
                        weekDates = weekDates
                    )
                }
            }
        }
    }

    private fun mealsForDate(date: LocalDate): List<MealLog> =
        weekMeals.filter { logDate(it.loggedAt) == date }

    private fun logDate(loggedAt: String?): LocalDate? =
        loggedAt?.let { runCatching { Instant.parse(it).atZone(ZoneId.systemDefault()).toLocalDate() }.getOrNull() }
}
