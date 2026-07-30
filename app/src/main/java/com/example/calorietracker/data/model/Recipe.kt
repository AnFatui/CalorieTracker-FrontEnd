package com.example.calorietracker.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String,
    @SerialName("category") val category: String,
    @SerialName("calories") val calories: Int,
    @SerialName("protein") val protein: Int,
    @SerialName("carbs") val carbs: Int,
    @SerialName("fat") val fat: Int,
    @SerialName("emoji") val emoji: String = "🍽",
    @SerialName("duration_minutes") val durationMinutes: Int? = null
)
