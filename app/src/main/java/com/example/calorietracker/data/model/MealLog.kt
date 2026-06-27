package com.example.calorietracker.data.model

data class MealLog(
    val name: String,
    val type: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)
