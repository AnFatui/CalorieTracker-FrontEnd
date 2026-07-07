package com.example.calorietracker.data.repository

import com.example.calorietracker.data.model.FastingSchedule
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class FastingScheduleRepository(
    private val supabaseClient: SupabaseClient
) {
    suspend fun getFastingSchedule(userId: String): FastingSchedule? {
        return supabaseClient.from("fasting_schedules")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }.decodeSingleOrNull<FastingSchedule>()
    }

    suspend fun upsertFastingSchedule(fastingInterval: FastingSchedule) {
        supabaseClient.from("fasting_schedules")
            .upsert(fastingInterval) {
                onConflict = "user_id"
            }
    }
}