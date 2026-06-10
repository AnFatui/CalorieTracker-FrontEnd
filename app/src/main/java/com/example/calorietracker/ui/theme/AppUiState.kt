package com.example.calorietracker.ui.theme

data class AppUiState (
    val name: String = "",

    val selectedGoal: String = "",

    val weeklyGoal: Float = 0.5f,
    val currentWeight: String = "",
    val targetWeight: String = "",

    val calories: Int = 0,
    val calorieGoal: Int = 2000,

    val protein: Int = 0,
    val proteinGoal: Int = 130,

    val carbs: Int = 0,
    val carbsGoal: Int = 250,

    val fat: Int = 0,
    val fatGoal: Int = 70,

    val waterMl: Int = 0,
    val waterGoalMl: Int = 2500,

    val steps: Int = 0,
    val stepGoal: Int = 10000
)