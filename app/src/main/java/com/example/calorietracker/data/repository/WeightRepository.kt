package com.example.calorietracker.data.repository

import com.example.calorietracker.data.model.AddWeightLogDTO
import com.example.calorietracker.data.model.WeightLog
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlin.time.ExperimentalTime

class WeightRepository(
    private val supabase: SupabaseClient
) {
    suspend fun getWeightLogs(userId: String): List<WeightLog> {
        return supabase.from("weight_logs")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }.decodeList<WeightLog>()
    }

    suspend fun addWeightLog(weightLog: AddWeightLogDTO) {
        supabase.from("weight_logs").insert(weightLog)
    }

    suspend fun updateWeightLog(id: String, weightKg: Double) {
        supabase.from("weight_logs").update(
            {
                set("weight_kg", weightKg)
            }
        ) {
            filter {
                eq("id", id)
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun getLatestWeightLog(userId: String): WeightLog? {
        return supabase.from("weight_logs")
            .select {
                filter { eq("user_id", userId) }
                order("logged_at", order = Order.DESCENDING)
                limit(1)
            }.decodeSingleOrNull<WeightLog>()
    }
}
