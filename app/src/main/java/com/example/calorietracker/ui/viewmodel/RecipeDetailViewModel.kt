package com.example.calorietracker.ui.viewmodel

import com.example.calorietracker.data.model.Recipe
import com.example.calorietracker.data.repository.RecipeRepository
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RecipeDetailUiState(
    override val loading: Boolean = false,
    override val error: String? = null,
    val recipe: Recipe? = null
) : UiState<RecipeDetailUiState> {
    override fun copyFlags(
        loading: Boolean,
        error: String?
    ): RecipeDetailUiState {
        return this.copy(loading = loading, error = error)
    }
}

class RecipeDetailViewModel(
    override val sessionManager: SessionManager,
    private val recipeRepository: RecipeRepository
) : BaseViewModel<RecipeDetailUiState>() {
    override val internalUiState = MutableStateFlow(RecipeDetailUiState())
    override val tag: String = "RecipeDetailViewModel"
    val uiState = internalUiState.asStateFlow()

    fun loadRecipe(recipeId: String) {
        tryAndLogScope {
            val recipe = recipeRepository.getRecipeById(recipeId)
            internalUiState.update { it.copy(recipe = recipe) }
        }
    }
}
