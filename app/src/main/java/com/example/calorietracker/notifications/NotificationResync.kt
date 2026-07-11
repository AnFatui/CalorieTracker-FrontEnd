package com.example.calorietracker.notifications

import com.example.calorietracker.data.local.NotificationPreferences
import com.example.calorietracker.data.repository.FastingScheduleRepository
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.flow.first

/**
 * Re-arms reminder schedules from persisted preferences.
 *
 * WorkManager's periodic work is supposed to survive reboots on its own, but several OEMs
 * (e.g. Samsung's background/auto-start restrictions) drop an app's persisted jobs on reboot
 * unless the app is explicitly allowed to auto-start. Calling this on every app launch, and
 * additionally from a BOOT_COMPLETED receiver where the OS permits it, makes the reminders
 * self-healing regardless of that OEM behavior.
 */
suspend fun resyncNotificationSchedules(
    preferences: NotificationPreferences,
    scheduler: NotificationScheduler,
    fastingScheduleRepository: FastingScheduleRepository,
    sessionManager: SessionManager
) {
    if (preferences.waterRemindersEnabled.first()) {
        scheduler.scheduleWaterReminders(preferences.waterReminderIntervalMinutes.first())
    }

    if (preferences.fastingRemindersEnabled.first()) {
        val userId = sessionManager.currentUserId
        if (userId != null) {
            val schedule = fastingScheduleRepository.getFastingSchedule(userId)
            if (schedule != null) {
                scheduler.scheduleFastingReminders(schedule)
            }
        }
    }
}
