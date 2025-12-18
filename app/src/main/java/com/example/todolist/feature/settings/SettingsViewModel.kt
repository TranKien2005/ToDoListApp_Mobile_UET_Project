package com.example.todolist.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.core.model.AppLanguage
import com.example.todolist.core.model.Settings
import com.example.todolist.domain.usecase.SettingsUseCases
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsUseCases: SettingsUseCases
) : ViewModel() {

    val settings: StateFlow<Settings> = settingsUseCases.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Settings() // Default settings
        )

    fun updateSettings(settings: Settings) {
        viewModelScope.launch {
            settingsUseCases.updateSettings(settings)
        }
    }

    fun updateLanguage(language: AppLanguage) {
        viewModelScope.launch {
            val current = settings.value
            settingsUseCases.updateSettings(current.copy(language = language))
        }
    }

    fun updateTaskReminderMinutes(minutes: Int) {
        viewModelScope.launch {
            val current = settings.value
            settingsUseCases.updateSettings(current.copy(taskReminderMinutes = minutes))
        }
    }

    fun updateNotifyDailyMissions(enabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            settingsUseCases.updateSettings(current.copy(notifyDailyMissions = enabled))
        }
    }

    fun updateNotifyWeeklyMissions(enabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            settingsUseCases.updateSettings(current.copy(notifyWeeklyMissions = enabled))
        }
    }

    fun updateNotifyMonthlyMissions(enabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            settingsUseCases.updateSettings(current.copy(notifyMonthlyMissions = enabled))
        }
    }

    fun updateDailySummaryHour(hour: Int) {
        viewModelScope.launch {
            val current = settings.value
            settingsUseCases.updateSettings(current.copy(dailySummaryHour = hour))
        }
    }

    fun updateMissionDeadlineWarningMinutes(minutes: Int) {
        viewModelScope.launch {
            val current = settings.value
            settingsUseCases.updateSettings(current.copy(missionDeadlineWarningMinutes = minutes))
        }
    }

    fun updateOverdueNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            settingsUseCases.updateSettings(current.copy(overdueNotificationEnabled = enabled))
        }
    }
}
