package com.example.calorietracker.data.repository

import com.example.calorietracker.data.model.MealLog
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MealRepository(
    private val supabase: SupabaseClient
) {

    suspend fun addMealLog(mealLog: MealLog) = withContext(Dispatchers.IO) {
        supabase.from("meal_logs").insert(mealLog)
    }

    suspend fun getTodayMealLogs(userId: String): List<MealLog> = withContext(Dispatchers.IO) {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val startOfDay = "${today}T00:00:00Z"

        supabase.from("meal_logs")
            .select {
                filter {
                    eq("user_id", userId)
                    gte("logged_at", startOfDay)
                }
            }.decodeList<MealLog>()
    }

    suspend fun getMealLogsInRange(
        userId: String,
        startInclusive: LocalDate,
        endExclusive: LocalDate
    ): List<MealLog> = withContext(Dispatchers.IO) {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val startIso = "${startInclusive.format(formatter)}T00:00:00Z"
        val endIso = "${endExclusive.format(formatter)}T00:00:00Z"

        supabase.from("meal_logs")
            .select {
                filter {
                    eq("user_id", userId)
                    gte("logged_at", startIso)
                    lt("logged_at", endIso)
                }
            }.decodeList<MealLog>()
    }
}
