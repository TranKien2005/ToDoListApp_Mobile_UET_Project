package com.example.todolist.data.mapper

import com.example.todolist.data.local.entity.TaskEntity
import com.example.todolist.core.model.Task
import com.example.todolist.core.model.RepeatType
import com.example.todolist.util.extension.DateExt
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

object TaskEntityMapper {
    private val json = Json { ignoreUnknownKeys = true }

    fun toDomain(entity: TaskEntity): Task {
        val imagesList = entity.images?.let {
            try {
                json.decodeFromString<List<String>>(it)
            } catch (_: Exception) {
                emptyList()
            }
        } ?: emptyList()

        return Task(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            startTime = DateExt.toLocalDateTime(entity.startTimeEpoch),
            durationMinutes = entity.durationMinutes,
            repeatType = try { RepeatType.valueOf(entity.repeatType) } catch (_: Exception) { RepeatType.NONE },
            images = imagesList
        )
    }

    fun fromDomain(task: Task): TaskEntity {
        val imagesJson = if (task.images.isNotEmpty()) {
            json.encodeToString(task.images)
        } else {
            null
        }

        return TaskEntity(
            id = task.id,
            title = task.title,
            description = task.description,
            startTimeEpoch = DateExt.toEpochMillis(task.startTime),
            durationMinutes = task.durationMinutes,
            repeatType = task.repeatType.name,
            images = imagesJson
        )
    }
}

