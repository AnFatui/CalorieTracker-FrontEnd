package com.example.calorietracker.ui.viewmodel

import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow

data class RecipeDetailUiState(
    override val loading: Boolean = false,
    override val error: String? = null
) : UiState<RecipeDetailUiState> {
    override fun copyFlags(
        loading: Boolean,
        error: String?
    ): RecipeDetailUiState {
        return this.copyFlags(loading = loading, error = error)
    }
}

class RecipeDetailViewModel(
    override val sessionManager: SessionManager
) : BaseViewModel<RecipeDetailUiState>() {
    override val internalUiState = MutableStateFlow(RecipeDetailUiState())
    override val tag: String = "RecipeDetailViewModel"
}
