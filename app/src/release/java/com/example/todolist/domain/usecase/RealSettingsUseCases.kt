package com.example.todolist.domain.usecase

import com.example.todolist.core.model.Settings
import com.example.todolist.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

// Release implementations that use real SettingsRepository
class RealGetSettingsUseCase(
    private val repository: SettingsRepository
) : GetSettingsUseCase {
    override operator fun invoke(): Flow<Settings> = repository.getSettings()
}

class RealUpdateSettingsUseCase(
    private val repository: SettingsRepository
) : UpdateSettingsUseCase {
    override suspend operator fun invoke(settings: Settings) {
        repository.updateSettings(settings)
    }
}

