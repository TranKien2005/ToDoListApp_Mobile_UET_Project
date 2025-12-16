package com.example.todolist.data.mapper

import com.example.todolist.data.local.entity.MissionEntity
import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.MissionStoredStatus
import com.example.todolist.util.extension.DateExt
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

object MissionEntityMapper {
    private val json = Json { ignoreUnknownKeys = true }

    fun toDomain(entity: MissionEntity): Mission {
        val imagesList = entity.images?.let {
            try {
                json.decodeFromString<List<String>>(it)
            } catch (_: Exception) {
                emptyList()
            }
        } ?: emptyList()

        return Mission(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            deadline = DateExt.toLocalDateTime(entity.deadlineEpoch),
            storedStatus = try {
                MissionStoredStatus.valueOf(entity.status)
            } catch (_: Exception) {
                MissionStoredStatus.UNSPECIFIED
            },
            images = imagesList
        )
    }

    fun fromDomain(mission: Mission): MissionEntity {
        val imagesJson = if (mission.images.isNotEmpty()) {
            json.encodeToString(mission.images)
        } else {
            null
        }

        return MissionEntity(
            id = mission.id,
            title = mission.title,
            description = mission.description,
            deadlineEpoch = DateExt.toEpochMillis(mission.deadline),
            status = mission.storedStatus.name,
            images = imagesJson
        )
    }
}

