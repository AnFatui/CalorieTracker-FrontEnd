package com.example.calorietracker.notifications

import com.example.calorietracker.data.local.NotificationPreferences
import com.example.calorietracker.data.repository.FastingScheduleRepository
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.flow.first

/**
 * Re-arms reminder schedules from persisted preferences.
 *
 * Exact alarms don't survive a device reboot (the OS clears all of an app's pending alarms), so
 * this re-creates them from persisted preferences on every app launch and additionally from a
 * BOOT_COMPLETED receiver, making the reminders self-healing.
 *
 * The water reminder uses [NotificationScheduler.ensureWaterRemindersScheduled] rather than
 * always rescheduling: its next trigger is "now + interval", so unconditionally re-arming it on
 * every app open would keep pushing the reminder further into the future and it would never fire
 * while the app is closed.
 */
suspend fun resyncNotificationSchedules(
    preferences: NotificationPreferences,
    scheduler: NotificationScheduler,
    fastingScheduleRepository: FastingScheduleRepository,
    sessionManager: SessionManager
) {
    if (preferences.waterRemindersEnabled.first()) {
        scheduler.ensureWaterRemindersScheduled(preferences.waterReminderIntervalMinutes.first())
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
