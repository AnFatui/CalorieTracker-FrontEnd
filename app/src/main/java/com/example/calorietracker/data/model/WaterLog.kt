package com.example.calorietracker.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WaterLog(
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("amount_ml") val amountMl: Int,
    @SerialName("logged_at") val loggedAt: String? = null
)
