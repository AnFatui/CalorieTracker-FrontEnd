package com.example.calorietracker.data.repository

import com.example.calorietracker.data.model.WeightLog
import com.example.calorietracker.providers.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeightRepository {

    suspend fun getWeightLogs(userId: String): List<WeightLog> = withContext(Dispatchers.IO) {
        try {
            SupabaseClientProvider.supabase.from("weight_logs")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeList<WeightLog>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addWeightLog(weightLog: WeightLog) = withContext(Dispatchers.IO) {
        SupabaseClientProvider.supabase.from("weight_logs").insert(weightLog)
    }
}
