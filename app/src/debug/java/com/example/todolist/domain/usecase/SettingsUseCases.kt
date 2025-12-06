package com.example.todolist.domain.usecase

import com.example.todolist.core.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// Debug-only fake implementations of settings use-cases. These live in the `debug` source set
// so they will be compiled into debug builds and can be swapped for real implementations in release.

private val _settingsState = MutableStateFlow(
    Settings(
        id = 1,
        taskReminderMinutes = 15,
        notifyDailyMissions = true,
        notifyWeeklyMissions = true,
        notifyMonthlyMissions = true,
        dailySummaryHour = 7,
        missionDeadlineWarningMinutes = 60,
        overdueNotificationEnabled = true
    )
)

class FakeGetSettingsUseCase : GetSettingsUseCase {
    override operator fun invoke(): Flow<Settings> = _settingsState
}

class FakeUpdateSettingsUseCase : UpdateSettingsUseCase {
    override suspend operator fun invoke(settings: Settings) {
        _settingsState.value = settings
    }
}

// Aggregator instance for debug builds
val fakeSettingsUseCases = SettingsUseCases(
    getSettings = FakeGetSettingsUseCase(),
    updateSettings = FakeUpdateSettingsUseCase()
)
