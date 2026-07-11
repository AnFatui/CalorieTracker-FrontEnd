package com.example.calorietracker.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.calorietracker.data.local.NotificationPreferences
import com.example.calorietracker.data.repository.FastingScheduleRepository
import com.example.calorietracker.notifications.NotificationScheduler
import com.example.calorietracker.notifications.WaterReminderInterval
import com.example.calorietracker.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationSettingsUiState(
    override val loading: Boolean = false,
    override val error: String? = null,
    val waterRemindersEnabled: Boolean = false,
    val waterReminderInterval: WaterReminderInterval = WaterReminderInterval.DEFAULT,
    val fastingRemindersEnabled: Boolean = false
) : UiState<NotificationSettingsUiState> {
    override fun copyFlags(loading: Boolean, error: String?): NotificationSettingsUiState {
        return this.copy(loading = loading, error = error)
    }
}

class NotificationSettingsViewModel(
    private val preferences: NotificationPreferences,
    private val scheduler: NotificationScheduler,
    private val fastingScheduleRepository: FastingScheduleRepository,
    override val sessionManager: SessionManager
) : BaseViewModel<NotificationSettingsUiState>() {
    override val internalUiState = MutableStateFlow(NotificationSettingsUiState())
    val uiState = internalUiState.asStateFlow()
    override val tag: String = "NotificationSettingsViewModel"

    init {
        viewModelScope.launch {
            combine(
                preferences.waterRemindersEnabled,
                preferences.waterReminderIntervalMinutes,
                preferences.fastingRemindersEnabled
            ) { waterEnabled, intervalMinutes, fastingEnabled ->
                Triple(waterEnabled, intervalMinutes, fastingEnabled)
            }.collect { (waterEnabled, intervalMinutes, fastingEnabled) ->
                internalUiState.update {
                    it.copy(
                        waterRemindersEnabled = waterEnabled,
                        waterReminderInterval = WaterReminderInterval.fromMinutes(intervalMinutes),
                        fastingRemindersEnabled = fastingEnabled
                    )
                }
            }
        }
    }

    fun setWaterRemindersEnabled(enabled: Boolean) {
        tryAndLogScope {
            preferences.setWaterRemindersEnabled(enabled)
            if (enabled) {
                scheduler.scheduleWaterReminders(preferences.waterReminderIntervalMinutes.first())
            } else {
                scheduler.cancelWaterReminders()
            }
        }
    }

    fun setWaterReminderInterval(interval: WaterReminderInterval) {
        tryAndLogScope {
            preferences.setWaterReminderIntervalMinutes(interval.minutes)
            if (preferences.waterRemindersEnabled.first()) {
                scheduler.scheduleWaterReminders(interval.minutes)
            }
        }
    }

    fun setFastingRemindersEnabled(enabled: Boolean) {
        tryAndLogScope {
            preferences.setFastingRemindersEnabled(enabled)
            if (enabled) {
                getUserId { userId ->
                    val schedule = fastingScheduleRepository.getFastingSchedule(userId)
                    if (schedule != null) {
                        scheduler.scheduleFastingReminders(schedule)
                    }
                }
            } else {
                scheduler.cancelFastingReminders()
            }
        }
    }
}
