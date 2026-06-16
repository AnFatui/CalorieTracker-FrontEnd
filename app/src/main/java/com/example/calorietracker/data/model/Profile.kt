package com.example.calorietracker.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    @SerialName("id") val id: String,
    @SerialName("user_name") val username: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("height_cm") val heightCm: Int? = null,
    @SerialName("weight_goal_kg") val targetWeightKg: Double? = null,
    @SerialName("birthdate") val birthdate: String? = null,
    @SerialName("sex") val sex: String? = null,
    @SerialName("activity_level") val activityLevel: String? = null,
    @SerialName("selected_goal") val selectedGoal: String? = null,
    @SerialName("weekly_goal") val weeklyGoal: Double? = null,
    @SerialName("calorie_goal") val calorieGoal: Int? = null,
    @SerialName("protein_goal") val proteinGoal: Int? = null,
    @SerialName("carbs_goal") val carbsGoal: Int? = null,
    @SerialName("fat_goal") val fatGoal: Int? = null
)
