package com.example.todolist.data.repository

import com.example.todolist.data.local.dao.SettingsDao
import com.example.todolist.data.mapper.SettingsEntityMapper
import com.example.todolist.domain.repository.SettingsRepository
import com.example.todolist.core.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class RoomSettingsRepositoryImpl(
    private val settingsDao: SettingsDao
) : SettingsRepository {

    // Cache settings mặc định để tránh tạo instance mới mỗi lần
    private val defaultSettings = Settings()

    override fun getSettings(): Flow<Settings> {
        return settingsDao.getSettings()
            .map { entity ->
                entity?.let { SettingsEntityMapper.toDomain(it) } ?: defaultSettings
            }
            .distinctUntilChanged() // Chỉ emit khi có thay đổi thực sự
    }

    override suspend fun updateSettings(settings: Settings) {
        val entity = SettingsEntityMapper.fromDomain(settings)
        // Dùng insertSettings với REPLACE để đảm bảo luôn có dữ liệu
        settingsDao.insertSettings(entity)
    }
}
