package com.example.calorietracker.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.notificationDataStore by preferencesDataStore(name = "notification_prefs")

class NotificationPreferences(private val context: Context) {

    private object Keys {
        val WATER_REMINDERS_ENABLED = booleanPreferencesKey("water_reminders_enabled")
        val WATER_REMINDER_INTERVAL_MINUTES = longPreferencesKey("water_reminder_interval_minutes")
        val FASTING_REMINDERS_ENABLED = booleanPreferencesKey("fasting_reminders_enabled")
    }

    val waterRemindersEnabled: Flow<Boolean> = context.notificationDataStore.data.map {
        it[Keys.WATER_REMINDERS_ENABLED] ?: false
    }

    val waterReminderIntervalMinutes: Flow<Long> = context.notificationDataStore.data.map {
        it[Keys.WATER_REMINDER_INTERVAL_MINUTES] ?: DEFAULT_WATER_INTERVAL_MINUTES
    }

    val fastingRemindersEnabled: Flow<Boolean> = context.notificationDataStore.data.map {
        it[Keys.FASTING_REMINDERS_ENABLED] ?: false
    }

    suspend fun setWaterRemindersEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { it[Keys.WATER_REMINDERS_ENABLED] = enabled }
    }

    suspend fun setWaterReminderIntervalMinutes(minutes: Long) {
        context.notificationDataStore.edit { it[Keys.WATER_REMINDER_INTERVAL_MINUTES] = minutes }
    }

    suspend fun setFastingRemindersEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { it[Keys.FASTING_REMINDERS_ENABLED] = enabled }
    }

    companion object {
        const val DEFAULT_WATER_INTERVAL_MINUTES = 60L
    }
}
