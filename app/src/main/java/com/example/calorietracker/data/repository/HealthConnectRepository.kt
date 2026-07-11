package com.example.calorietracker.data.repository

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.LocalDate
import java.time.Period

class HealthConnectRepository(private val context: Context) {

    companion object {
        private const val TAG = "HealthConnectRepository"
        val STEP_PERMISSIONS = setOf(HealthPermission.getReadPermission(StepsRecord::class))
    }

    fun isAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }

    private fun getClientOrNull(): HealthConnectClient? {
        if (!isAvailable()) return null
        return try {
            HealthConnectClient.getOrCreate(context)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create HealthConnectClient", e)
            null
        }
    }

    suspend fun hasStepsPermission(): Boolean {
        val client = getClientOrNull() ?: return false
        return try {
            client.permissionController.getGrantedPermissions().containsAll(STEP_PERMISSIONS)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read granted permissions", e)
            false
        }
    }

    suspend fun getTodaySteps(): Int {
        val today = LocalDate.now()
        return getStepsInRange(today, today.plusDays(1))[today] ?: 0
    }

    suspend fun getStepsInRange(startInclusive: LocalDate, endExclusive: LocalDate): Map<LocalDate, Int> {
        val client = getClientOrNull() ?: return emptyMap()
        if (!hasStepsPermission()) return emptyMap()

        return try {
            val response = client.aggregateGroupByPeriod(
                AggregateGroupByPeriodRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(
                        startInclusive.atStartOfDay(),
                        endExclusive.atStartOfDay()
                    ),
                    timeRangeSlicer = Period.ofDays(1)
                )
            )
            response.associate { bucket ->
                bucket.startTime.toLocalDate() to (bucket.result[StepsRecord.COUNT_TOTAL] ?: 0L).toInt()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to aggregate steps in range", e)
            emptyMap()
        }
    }
}
