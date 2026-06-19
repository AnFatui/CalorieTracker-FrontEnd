package com.example.calorietracker.ui.viewmodel

import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow

data class RecipesUiState(
    override val loading: Boolean = false,
    override val error: String? = null
) : UiState<RecipesUiState> {
    override fun copyFlags(
        loading: Boolean,
        error: String?
    ): RecipesUiState {
        return this.copyFlags(loading = loading, error = error)
    }
}

class RecipesViewModel(
    override val sessionManager: SessionManager
) : BaseViewModel<RecipesUiState>() {
    override val internalUiState = MutableStateFlow(RecipesUiState())
    override val tag: String = "RecipesViewModel"
}
