package com.example.todolist.domain.usecase

import com.example.todolist.core.model.Settings
import kotlinx.coroutines.flow.Flow

// Keep only interface definitions in `main`. Concrete implementations must live in debug/release.
interface GetSettingsUseCase {
    operator fun invoke(): Flow<Settings>
}

interface UpdateSettingsUseCase {
    suspend operator fun invoke(settings: Settings)
}

// Aggregator contains only the interfaces; implementations come from debug/release.
data class SettingsUseCases(
    val getSettings: GetSettingsUseCase,
    val updateSettings: UpdateSettingsUseCase
)

