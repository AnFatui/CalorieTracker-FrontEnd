package com.example.calorietracker.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.calorietracker.data.model.FastingSchedule
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

enum class FastingReminderType {
    BEFORE_START,
    START,
    END
}

/**
 * Schedules and cancels local reminder notifications via AlarmManager.
 *
 * Exact alarms (setExactAndAllowWhileIdle) are used because both reminder types are meant to fire
 * at a specific wall-clock moment (e.g. fasting end at 11:00). A prior version used WorkManager's
 * PeriodicWorkRequest, which only guarantees a *minimum* delay, not an exact time: under
 * Doze/App Standby the OS deferred it to its next maintenance window, causing reminders to arrive
 * hours late or only once the app was reopened. Since exact alarms are one-shot, AlarmReceiver
 * reschedules each alarm's successor itself after it fires.
 */
class NotificationScheduler(private val context: Context) {

    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleWaterReminders(intervalMinutes: Long) {
        val triggerAtMillis = System.currentTimeMillis() + intervalMinutes * 60_000L
        scheduleExactAlarm(WATER_REQUEST_CODE, triggerAtMillis) {
            putExtra(EXTRA_ALARM_TYPE, ALARM_TYPE_WATER)
        }
    }

    /**
     * Like [scheduleWaterReminders], but leaves an already-pending alarm untouched. Used when
     * re-arming reminders on app start/boot, so simply reopening the app doesn't keep pushing the
     * next water reminder further into the future.
     */
    fun ensureWaterRemindersScheduled(intervalMinutes: Long) {
        if (!isAlarmPending(WATER_REQUEST_CODE)) {
            scheduleWaterReminders(intervalMinutes)
        }
    }

    fun cancelWaterReminders() {
        cancelAlarm(WATER_REQUEST_CODE)
    }

    fun scheduleFastingReminders(schedule: FastingSchedule) {
        val startMinutes = schedule.startTime.hour * 60 + schedule.startTime.minute
        val beforeStartMinutes = floorMod(startMinutes - 60, MINUTES_PER_DAY)
        val endMinutes = floorMod(startMinutes + schedule.durationHours * 60, MINUTES_PER_DAY)

        scheduleFastingAlarm(FASTING_BEFORE_START_REQUEST_CODE, minutesToLocalTime(beforeStartMinutes), FastingReminderType.BEFORE_START)
        scheduleFastingAlarm(FASTING_START_REQUEST_CODE, schedule.startTime, FastingReminderType.START)
        scheduleFastingAlarm(FASTING_END_REQUEST_CODE, minutesToLocalTime(endMinutes), FastingReminderType.END)
    }

    fun cancelFastingReminders() {
        cancelAlarm(FASTING_BEFORE_START_REQUEST_CODE)
        cancelAlarm(FASTING_START_REQUEST_CODE)
        cancelAlarm(FASTING_END_REQUEST_CODE)
    }

    private fun scheduleFastingAlarm(requestCode: Int, targetTime: LocalTime, type: FastingReminderType) {
        val triggerAtMillis = System.currentTimeMillis() + initialDelayMillis(targetTime)
        scheduleExactAlarm(requestCode, triggerAtMillis) {
            putExtra(EXTRA_ALARM_TYPE, ALARM_TYPE_FASTING)
            putExtra(EXTRA_FASTING_REMINDER_TYPE, type.name)
        }
    }

    private fun scheduleExactAlarm(requestCode: Int, triggerAtMillis: Long, putExtras: Intent.() -> Unit) {
        val pendingIntent = broadcastPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT, putExtras)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            // Exact-alarm permission not granted (Android 12+): fall back to an inexact alarm,
            // which the OS still honors far more closely than a periodic WorkManager job did.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun cancelAlarm(requestCode: Int) {
        val pendingIntent = broadcastPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT) {}
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun isAlarmPending(requestCode: Int): Boolean {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent != null
    }

    private fun broadcastPendingIntent(requestCode: Int, flags: Int, putExtras: Intent.() -> Unit): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply(putExtras)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            flags or PendingIntent.FLAG_IMMUTABLE
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

        const val EXTRA_ALARM_TYPE = "alarm_type"
        const val EXTRA_FASTING_REMINDER_TYPE = "fasting_reminder_type"
        const val ALARM_TYPE_WATER = "water"
        const val ALARM_TYPE_FASTING = "fasting"

        private const val WATER_REQUEST_CODE = 1
        private const val FASTING_BEFORE_START_REQUEST_CODE = 2
        private const val FASTING_START_REQUEST_CODE = 3
        private const val FASTING_END_REQUEST_CODE = 4
    }
}
