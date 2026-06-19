package com.example.calorietracker.ui.viewmodel

import com.example.calorietracker.data.model.MealLog
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MealTrackingUiDataState(
    val meals: List<MealLog> = emptyList()
)

data class MealTrackingUiState(
    override val loading: Boolean = false,
    override val error: String? = null
) : UiState<MealTrackingUiState> {
    override fun copyFlags(
        loading: Boolean,
        error: String?
    ): MealTrackingUiState {
        return this.copyFlags(loading = loading, error = error)
    }
}

class MealTrackingViewModel(
    override val sessionManager: SessionManager
) : BaseViewModel<MealTrackingUiState>() {
    override val internalUiState = MutableStateFlow(MealTrackingUiState())
    override val tag: String = "MealTrackingViewModel"

    val uiState = internalUiState.asStateFlow()
    private val _uiDataState = MutableStateFlow(MealTrackingUiDataState())
    val uiDataState: StateFlow<MealTrackingUiDataState> = _uiDataState.asStateFlow()
}
