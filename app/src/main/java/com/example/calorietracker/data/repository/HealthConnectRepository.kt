package com.example.calorietracker.data.repository

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.ZoneId
import java.time.ZonedDateTime

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
        val client = getClientOrNull() ?: return 0
        if (!hasStepsPermission()) return 0

        return try {
            val zoneId = ZoneId.systemDefault()
            val now = ZonedDateTime.now(zoneId)
            val startOfDay = now.toLocalDate().atStartOfDay(zoneId)

            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startOfDay.toInstant(), now.toInstant())
                )
            )
            (response[StepsRecord.COUNT_TOTAL] ?: 0L).toInt()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to aggregate today's steps", e)
            0
        }
    }
}
