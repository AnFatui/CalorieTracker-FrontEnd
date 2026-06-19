package com.example.calorietracker.ui.viewmodel

import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    val waterMl: Int? = null,
    val waterMlGoal: Int? = null
) : UiState<StatisticsUiState> {
    override fun copyFlags(
        loading: Boolean,
        error: String?
    ): StatisticsUiState {
        return this.copy(loading = loading, error = error)
    }
}

class StatisticsViewModel(
    override val sessionManager: SessionManager
) : BaseViewModel<StatisticsUiState>() {
    override val tag: String = "StatisticsViewModel"

    override val internalUiState = MutableStateFlow(StatisticsUiState())
    val uiState = internalUiState.asStateFlow()


}
