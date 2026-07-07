package com.example.calorietracker.ui.viewmodel

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
    override val sessionManager: SessionManager
) : BaseViewModel<AddMealUiState>() {
    override val internalUiState = MutableStateFlow(AddMealUiState())
    override val tag: String = "AddMealViewModel"

    val uiState = internalUiState.asStateFlow()
}
