package com.example.calorietracker.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.calorietracker.data.model.FastingSchedule
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.util.concurrent.TimeUnit
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Schedules and cancels local reminder notifications via WorkManager.
 *
 * WorkManager (not AlarmManager) is used for both reminder types: neither water nor fasting
 * reminders require alarm-clock-level exactness (a few minutes of drift is acceptable), so we
 * avoid the SCHEDULE_EXACT_ALARM permission and manual boot-rescheduling that exact alarms would
 * require. WorkManager's periodic work persists across app kills and device reboots on its own.
 */
class NotificationScheduler(private val context: Context) {

    fun scheduleWaterReminders(intervalMinutes: Long) {
        val request = PeriodicWorkRequestBuilder<WaterReminderWorker>(intervalMinutes, TimeUnit.MINUTES)
            .setInitialDelay(intervalMinutes, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WATER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelWaterReminders() {
        WorkManager.getInstance(context).cancelUniqueWork(WATER_WORK_NAME)
    }

    fun scheduleFastingReminders(schedule: FastingSchedule) {
        val startMinutes = schedule.startTime.hour * 60 + schedule.startTime.minute
        val beforeStartMinutes = floorMod(startMinutes - 60, MINUTES_PER_DAY)
        val endMinutes = floorMod(startMinutes + schedule.durationHours * 60, MINUTES_PER_DAY)

        scheduleDaily(FASTING_BEFORE_START_WORK_NAME, minutesToLocalTime(beforeStartMinutes), FastingReminderType.BEFORE_START)
        scheduleDaily(FASTING_START_WORK_NAME, schedule.startTime, FastingReminderType.START)
        scheduleDaily(FASTING_END_WORK_NAME, minutesToLocalTime(endMinutes), FastingReminderType.END)
    }

    fun cancelFastingReminders() {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(FASTING_BEFORE_START_WORK_NAME)
        workManager.cancelUniqueWork(FASTING_START_WORK_NAME)
        workManager.cancelUniqueWork(FASTING_END_WORK_NAME)
    }

    private fun scheduleDaily(workName: String, targetTime: LocalTime, type: FastingReminderType) {
        val data = Data.Builder()
            .putString(FastingReminderWorker.KEY_REMINDER_TYPE, type.name)
            .build()

        val request = PeriodicWorkRequestBuilder<FastingReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMillis(targetTime), TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            workName,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    @OptIn(ExperimentalTime::class)
    private fun initialDelayMillis(targetTime: LocalTime): Long {
        val timeZone = TimeZone.currentSystemDefault()
        val now = Clock.System.now().toLocalDateTime(timeZone)

        var target = LocalDateTime(now.date, targetTime)
        if (target <= now) {
            target = LocalDateTime(now.date.plus(1, DateTimeUnit.DAY), targetTime)
        }

        val nowInstant = now.toInstant(timeZone)
        val targetInstant = target.toInstant(timeZone)
        return (targetInstant - nowInstant).inWholeMilliseconds
    }

    private fun minutesToLocalTime(totalMinutes: Int): LocalTime {
        val m = floorMod(totalMinutes, MINUTES_PER_DAY)
        return LocalTime(m / 60, m % 60)
    }

    private fun floorMod(x: Int, m: Int): Int = ((x % m) + m) % m

    companion object {
        private const val MINUTES_PER_DAY = 1440
        private const val WATER_WORK_NAME = "water_reminder_work"
        private const val FASTING_BEFORE_START_WORK_NAME = "fasting_reminder_before_start_work"
        private const val FASTING_START_WORK_NAME = "fasting_reminder_start_work"
        private const val FASTING_END_WORK_NAME = "fasting_reminder_end_work"
    }
}
