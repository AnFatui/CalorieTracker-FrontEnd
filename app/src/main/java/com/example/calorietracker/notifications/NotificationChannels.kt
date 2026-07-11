package com.example.calorietracker.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val WATER_CHANNEL_ID = "water_reminders"
    const val FASTING_CHANNEL_ID = "fasting_reminders"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val waterChannel = NotificationChannel(
            WATER_CHANNEL_ID,
            "Wasser-Erinnerungen",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Erinnerungen, regelmäßig Wasser zu trinken"
        }

        val fastingChannel = NotificationChannel(
            FASTING_CHANNEL_ID,
            "Fasten-Erinnerungen",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Erinnerungen zu Beginn und Ende deines Fastenintervalls"
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannels(listOf(waterChannel, fastingChannel))
    }
}
