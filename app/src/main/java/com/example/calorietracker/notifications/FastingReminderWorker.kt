package com.example.calorietracker.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.calorietracker.R

enum class FastingReminderType {
    BEFORE_START,
    START,
    END
}

class FastingReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val typeName = inputData.getString(KEY_REMINDER_TYPE) ?: return Result.failure()
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

        val notification = NotificationCompat.Builder(applicationContext, NotificationChannels.FASTING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(notificationId, notification)
        return Result.success()
    }

    companion object {
        const val KEY_REMINDER_TYPE = "reminder_type"
        const val FASTING_BEFORE_START_NOTIFICATION_ID = 2001
        const val FASTING_START_NOTIFICATION_ID = 2002
        const val FASTING_END_NOTIFICATION_ID = 2003
    }
}
