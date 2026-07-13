package com.example.calorietracker.data.repository

import com.example.calorietracker.data.model.MealLog
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

class MealRepository(
    private val supabase: SupabaseClient
) {

    suspend fun addMealLog(mealLog: MealLog) = withContext(Dispatchers.IO) {
        supabase.from("meal_logs").insert(mealLog)
    }

    suspend fun getTodayMealLogs(userId: String): List<MealLog> = withContext(Dispatchers.IO) {
        val startOfDay = startOfDayUtc(LocalDate.now())

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
        val startIso = startOfDayUtc(startInclusive)
        val endIso = startOfDayUtc(endExclusive)

        supabase.from("meal_logs")
            .select {
                filter {
                    eq("user_id", userId)
                    gte("logged_at", startIso)
                    lt("logged_at", endIso)
                }
            }.decodeList<MealLog>()
    }

    suspend fun deleteMealLog(logId: String) = withContext(Dispatchers.IO) {
        supabase.from("meal_logs").delete {
            filter {
                eq("id", logId)
            }
        }
    }
}

private fun startOfDayUtc(date: LocalDate): String =
    date.atStartOfDay(ZoneId.systemDefault()).toInstant().toString()
