package com.example.calorietracker.data.repository

import com.example.calorietracker.data.model.WaterLog
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

class WaterRepository(
    private val supabase: SupabaseClient
) {

    suspend fun addWaterLog(waterLog: WaterLog) = withContext(Dispatchers.IO) {
        supabase.from("water_logs").insert(waterLog)
    }

    suspend fun getPresentWaterLogs(userId: String): List<WaterLog> = withContext(Dispatchers.IO) {
        val startOfDay = startOfDayUtc(LocalDate.now())

        supabase.from("water_logs")
            .select {
                filter {
                    eq("user_id", userId)
                    gte("logged_at", startOfDay)
                }
            }.decodeList<WaterLog>()
    }

    suspend fun getWaterLogsInRange(
        userId: String,
        startInclusive: LocalDate,
        endExclusive: LocalDate
    ): List<WaterLog> = withContext(Dispatchers.IO) {
        val startIso = startOfDayUtc(startInclusive)
        val endIso = startOfDayUtc(endExclusive)

        supabase.from("water_logs")
            .select {
                filter {
                    eq("user_id", userId)
                    gte("logged_at", startIso)
                    lt("logged_at", endIso)
                }
            }.decodeList<WaterLog>()
    }

    suspend fun deleteWaterLog(logId: String) = withContext(Dispatchers.IO) {
        supabase.from("water_logs").delete {
            filter {
                eq("id", logId)
            }
        }
    }

    suspend fun deleteAllWaterLogsForToday(userId: String) = withContext(Dispatchers.IO) {
        val startOfDay = startOfDayUtc(LocalDate.now())

        supabase.from("water_logs").delete {
            filter {
                eq("user_id", userId)
                gte("logged_at", startOfDay)
            }
        }
    }
}

private fun startOfDayUtc(date: LocalDate): String =
    date.atStartOfDay(ZoneId.systemDefault()).toInstant().toString()
