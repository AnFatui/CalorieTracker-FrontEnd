package com.example.calorietracker.ui.viewmodel

interface UiState<T : UiState<T>> {
    val loading: Boolean
    val error: String?
    fun copyFlags(loading: Boolean = false, error: String? = null): T
}
