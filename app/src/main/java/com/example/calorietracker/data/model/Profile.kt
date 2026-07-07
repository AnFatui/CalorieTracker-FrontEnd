package com.example.calorietracker.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Profile(
    @SerialName("id") val id: String,
    @SerialName("user_name") val username: String,
    @Transient val displayName: String? = null,
    @SerialName("height_cm") val heightCm: Int? = null,
    @SerialName("weight_goal_kg") val targetWeightKg: Double? = null,
    @SerialName("birth_year") val birthYear: Int? = null,
    @SerialName("metabolism_type") val metabolismType: MetabolismType = MetabolismType.MALE,
    @SerialName("activity_level") val activityLevel: ActivityLevel? = null,
    @SerialName("weight_strategy") val weightStrategy: WeightStrategy? = null,
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

@Serializable
enum class ActivityLevel {
    @SerialName("sedentary")
    SEDENTARY,

    @SerialName("lightly_active")
    LIGHTLY_ACTIVE,

    @SerialName("moderately_active")
    MODERATELY_ACTIVE,

    @SerialName("very_active")
    VERY_ACTIVE,

    @SerialName("extremely_active")
    EXTREMELY_ACTIVE
}

@Serializable
enum class WeightStrategy {
    @SerialName("lose_weight")
    LOSE_WEIGHT,

    @SerialName("maintain_weight")
    MAINTAIN_WEIGHT,

    @SerialName("gain_weight")
    GAIN_WEIGHT

}

@Serializable
enum class MetabolismType {
    @SerialName("male")
    MALE,

    @SerialName("female")
    FEMALE
}