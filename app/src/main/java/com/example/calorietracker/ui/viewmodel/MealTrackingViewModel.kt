package com.example.calorietracker.ui.viewmodel

import com.example.calorietracker.data.model.MealLog
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MealTrackingUiState(
    override val loading: Boolean = false,
    override val error: String? = null,
    val meals: List<MealLog> = emptyList()
) : UiState<MealTrackingUiState> {
    override fun copyFlags(
        loading: Boolean,
        error: String?
    ): MealTrackingUiState {
        return this.copy(loading = loading, error = error)
    }
}

class MealTrackingViewModel(
    override val sessionManager: SessionManager
) : BaseViewModel<MealTrackingUiState>() {
    override val internalUiState = MutableStateFlow(MealTrackingUiState())
    override val tag: String = "MealTrackingViewModel"

    val uiState = internalUiState.asStateFlow()
}
