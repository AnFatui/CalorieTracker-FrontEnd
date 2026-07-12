package com.example.calorietracker.notifications

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.calorietracker.R
import com.example.calorietracker.data.local.NotificationPreferences
import com.example.calorietracker.data.repository.FastingScheduleRepository
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Receives the exact alarms fired by [NotificationScheduler], shows the corresponding
 * notification, and re-arms the next occurrence (exact alarms are one-shot).
 */
class AlarmReceiver : BroadcastReceiver(), KoinComponent {

    private val preferences: NotificationPreferences by inject()
    private val scheduler: NotificationScheduler by inject()
    private val fastingScheduleRepository: FastingScheduleRepository by inject()
    private val sessionManager: SessionManager by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.getStringExtra(NotificationScheduler.EXTRA_ALARM_TYPE)) {
                    NotificationScheduler.ALARM_TYPE_WATER -> handleWaterAlarm(context)
                    NotificationScheduler.ALARM_TYPE_FASTING -> handleFastingAlarm(context, intent)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleWaterAlarm(context: Context) {
        if (!preferences.waterRemindersEnabled.first()) return

        showNotification(
            context,
            NotificationChannels.WATER_CHANNEL_ID,
            WATER_NOTIFICATION_ID,
            "Zeit zu trinken",
            "Vergiss nicht, Wasser zu trinken!"
        )

        scheduler.scheduleWaterReminders(preferences.waterReminderIntervalMinutes.first())
    }

    private suspend fun handleFastingAlarm(context: Context, intent: Intent) {
        if (!preferences.fastingRemindersEnabled.first()) return

        val typeName = intent.getStringExtra(NotificationScheduler.EXTRA_FASTING_REMINDER_TYPE) ?: return
        val type = FastingReminderType.valueOf(typeName)

        val (title, text, notificationId) = when (type) {
            FastingReminderType.BEFORE_START -> Triple(
                "Fasten beginnt bald",
                "Dein Fastenintervall beginnt in einer Stunde.",
                FASTING_BEFORE_START_NOTIFICATION_ID
            )
            FastingReminderType.START -> Triple(
                "Fasten hat begonnen",
                "Dein Fastenintervall hat jetzt begonnen.",
                FASTING_START_NOTIFICATION_ID
            )
            FastingReminderType.END -> Triple(
                "Fasten beendet",
                "Dein Fastenintervall ist beendet, guten Appetit!",
                FASTING_END_NOTIFICATION_ID
            )
        }

        showNotification(context, NotificationChannels.FASTING_CHANNEL_ID, notificationId, title, text)

        val userId = sessionManager.currentUserId ?: return
        val schedule = fastingScheduleRepository.getFastingSchedule(userId) ?: return
        scheduler.scheduleFastingReminders(schedule)
    }

    private fun showNotification(context: Context, channelId: String, notificationId: Int, title: String, text: String) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    companion object {
        const val WATER_NOTIFICATION_ID = 1001
        const val FASTING_BEFORE_START_NOTIFICATION_ID = 2001
        const val FASTING_START_NOTIFICATION_ID = 2002
        const val FASTING_END_NOTIFICATION_ID = 2003
    }
}
