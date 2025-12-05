package com.example.todolist.data.mapper

import com.example.todolist.data.local.entity.TaskEntity
import com.example.todolist.core.model.Task
import com.example.todolist.core.model.RepeatType
import com.example.todolist.util.extension.DateExt

object TaskEntityMapper {
    fun toDomain(entity: TaskEntity): Task {
        return Task(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            startTime = DateExt.toLocalDateTime(entity.startTimeEpoch),
            durationMinutes = entity.durationMinutes,
            repeatType = try { RepeatType.valueOf(entity.repeatType) } catch (_: Exception) { RepeatType.NONE }
        )
    }

    fun fromDomain(task: Task): TaskEntity {
        return TaskEntity(
            id = task.id,
            title = task.title,
            description = task.description,
            startTimeEpoch = DateExt.toEpochMillis(task.startTime),
            durationMinutes = task.durationMinutes,
            repeatType = task.repeatType.name
        )
    }
}
