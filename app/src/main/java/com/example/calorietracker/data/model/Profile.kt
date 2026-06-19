package com.example.calorietracker.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Profile(
    @SerialName("id") val id: String,
    @SerialName("user_name") val username: String? = null,
    @Transient val displayName: String? = null,
    @SerialName("height_cm") val heightCm: Int? = null,
    @SerialName("weight_goal_kg") val targetWeightKg: Double? = null,
    @SerialName("birth_year") val birthYear: Int? = null,
    @SerialName("sex") val sex: String? = null,
    @SerialName("activity_level") val activityLevel: String? = null,
    @SerialName("selected_goal") val selectedGoal: String? = null,
    @SerialName("weekly_goal") val weeklyGoal: Double? = null,
    @SerialName("calorie_goal") val calorieGoal: Int? = null,
    @SerialName("protein_goal") val proteinGoal: Int? = null,
    @SerialName("carbs_goal") val carbsGoal: Int? = null,
    @SerialName("fat_goal") val fatGoal: Int? = null,
    @SerialName("water_goal_ml") val waterGoalMl: Int? = null,
    @SerialName("daily_step_goal") val dailyStepGoal: Int? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("onboarding_done") val onboardingDone: Boolean = false
)
