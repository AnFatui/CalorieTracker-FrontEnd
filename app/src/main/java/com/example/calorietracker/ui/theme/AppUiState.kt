package com.example.calorietracker.ui.theme

data class MealEntry(
    val name: String,
    val mealType: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)

data class WeightEntry(
    val value: String,
    val dateLabel: String
)

data class AppUiState(
    val name: String = "",
    val email: String = "",

    val profileImageUri: String? = null,

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
    val weeklyWaterMl: List<Int> = listOf(0, 0, 0, 0, 0, 0, 0),

    val steps: Int = 0,
    val stepGoal: Int = 10000,

    val meals: List<MealEntry> = emptyList(),
    val weightEntries: List<WeightEntry> = emptyList(),

    val waterReminderEnabled: Boolean = false,
    val fastingReminderEnabled: Boolean = false,

    val fastingStartTime: String = "",
    val fastingEndTime: String = ""
)