package com.example.calorietracker.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

@Serializable
data class WeightLog @OptIn(ExperimentalTime::class) constructor(
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("weight_kg") val weightKg: Double,
    @SerialName("logged_at") val loggedAt: Instant
)

@Serializable
data class AddWeightLogDTO @OptIn(ExperimentalTime::class) constructor(
    @SerialName("user_id") val userId: String,
    @SerialName("weight_kg") val weightKg: Double,
    @SerialName("logged_at") val loggedAt: Instant
)
