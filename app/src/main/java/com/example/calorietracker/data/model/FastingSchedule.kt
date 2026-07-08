package com.example.calorietracker.data.model

import kotlinx.datetime.LocalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

@Serializable
data class FastingSchedule @OptIn(ExperimentalTime::class) constructor(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("duration_hours") val durationHours: Int,
    @SerialName("start_time") val startTime: LocalTime,
    @SerialName("is_active") val isActive: Boolean = true
)