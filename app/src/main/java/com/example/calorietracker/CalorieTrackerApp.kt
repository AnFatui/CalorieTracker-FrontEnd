package com.example.calorietracker

import android.app.Application
import com.example.calorietracker.data.local.NotificationPreferences
import com.example.calorietracker.data.repository.FastingScheduleRepository
import com.example.calorietracker.notifications.NotificationChannels
import com.example.calorietracker.notifications.NotificationScheduler
import com.example.calorietracker.notifications.resyncNotificationSchedules
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin

class CalorieTrackerApp : Application(), KoinComponent {

    private val notificationPreferences: NotificationPreferences by inject()
    private val notificationScheduler: NotificationScheduler by inject()
    private val fastingScheduleRepository: FastingScheduleRepository by inject()
    private val sessionManager: SessionManager by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CalorieTrackerApp)
            modules(appModule)
        }
        NotificationChannels.createChannels(this)

        // Self-heals reminder scheduling on every app start, since some OEMs drop WorkManager's
        // persisted jobs across a reboot despite it being designed to survive one on its own.
        CoroutineScope(Dispatchers.IO).launch {
            resyncNotificationSchedules(
                notificationPreferences,
                notificationScheduler,
                fastingScheduleRepository,
                sessionManager
            )
        }
    }
}
