package com.example.calorietracker.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MealLog(
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("name") val name: String,
    @SerialName("type") val type: String,
    @SerialName("calories") val calories: Int,
    @SerialName("protein") val protein: Int,
    @SerialName("carbs") val carbs: Int,
    @SerialName("fat") val fat: Int,
    @SerialName("logged_at") val loggedAt: String? = null
)
