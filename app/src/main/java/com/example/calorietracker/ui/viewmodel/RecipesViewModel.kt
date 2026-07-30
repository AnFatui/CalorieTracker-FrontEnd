package com.example.calorietracker.ui.viewmodel

import com.example.calorietracker.data.model.Recipe
import com.example.calorietracker.data.repository.RecipeRepository
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RecipesUiState(
    override val loading: Boolean = false,
    override val error: String? = null,
    val recipes: List<Recipe> = emptyList()
) : UiState<RecipesUiState> {
    override fun copyFlags(
        loading: Boolean,
        error: String?
    ): RecipesUiState {
        return this.copy(loading = loading, error = error)
    }
}

class RecipesViewModel(
    override val sessionManager: SessionManager,
    private val recipeRepository: RecipeRepository
) : BaseViewModel<RecipesUiState>() {
    override val internalUiState = MutableStateFlow(RecipesUiState())
    override val tag: String = "RecipesViewModel"
    val uiState = internalUiState.asStateFlow()

    init {
        loadRecipes()
    }

    private fun loadRecipes() {
        tryAndLogScope {
            val recipes = recipeRepository.getRecipes()
            internalUiState.update { it.copy(recipes = recipes) }
        }
    }
}
