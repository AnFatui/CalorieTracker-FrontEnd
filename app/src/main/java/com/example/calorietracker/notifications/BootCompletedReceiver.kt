package com.example.calorietracker.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.calorietracker.data.local.NotificationPreferences
import com.example.calorietracker.data.repository.FastingScheduleRepository
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BootCompletedReceiver : BroadcastReceiver(), KoinComponent {

    private val preferences: NotificationPreferences by inject()
    private val scheduler: NotificationScheduler by inject()
    private val fastingScheduleRepository: FastingScheduleRepository by inject()
    private val sessionManager: SessionManager by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                resyncNotificationSchedules(preferences, scheduler, fastingScheduleRepository, sessionManager)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
