package com.example.todolist.data.repository

import com.example.todolist.data.local.dao.SettingsDao
import com.example.todolist.data.mapper.SettingsEntityMapper
import com.example.todolist.domain.repository.SettingsRepository
import com.example.todolist.core.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomSettingsRepositoryImpl(
    private val settingsDao: SettingsDao
) : SettingsRepository {

    override fun getSettings(): Flow<Settings> {
        return settingsDao.getSettings().map { entity ->
            entity?.let { SettingsEntityMapper.toDomain(it) }
                ?: Settings() // Trả về settings mặc định nếu chưa có trong DB
        }
    }

    override suspend fun updateSettings(settings: Settings) {
        val entity = SettingsEntityMapper.fromDomain(settings)
        settingsDao.updateSettings(entity)
    }
}

