package com.example.calorietracker.ui.viewmodel

import com.example.calorietracker.data.model.MealLog
import com.example.calorietracker.data.repository.MealRepository
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AddMealUiState(
    override val loading: Boolean = false,
    override val error: String? = null
) : UiState<AddMealUiState> {
    override fun copyFlags(
        loading: Boolean,
        error: String?
    ): AddMealUiState {
        return this.copy(loading = loading, error = error)
    }
}

class AddMealViewModel(
    override val sessionManager: SessionManager,
    private val mealRepository: MealRepository
) : BaseViewModel<AddMealUiState>() {
    override val internalUiState = MutableStateFlow(AddMealUiState())
    override val tag: String = "AddMealViewModel"

    val uiState = internalUiState.asStateFlow()

    fun saveMeal(
        name: String,
        type: String,
        calories: Int,
        protein: Int,
        carbs: Int,
        fat: Int,
        onSaved: () -> Unit
    ) {
        tryAndLogScope {
            getUserId { userId ->
                mealRepository.addMealLog(
                    MealLog(
                        userId = userId,
                        name = name,
                        type = type,
                        calories = calories,
                        protein = protein,
                        carbs = carbs,
                        fat = fat
                    )
                )
                onSaved()
            }
        }
    }
}
