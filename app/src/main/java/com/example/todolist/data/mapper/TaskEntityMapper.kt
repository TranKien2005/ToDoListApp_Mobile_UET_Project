package com.example.todolist.data.mapper

import com.example.todolist.data.local.entity.TaskEntity
import com.example.todolist.domain.model.Task
import com.example.todolist.util.extension.DateExt

object TaskEntityMapper {
    fun toDomain(entity: TaskEntity): Task {
        return Task(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            startTime = DateExt.toLocalDateTime(entity.startTimeEpoch),
            endTime = entity.endTimeEpoch?.let { DateExt.toLocalDateTime(it) },
            isCompleted = entity.isCompleted
        )
    }

    fun fromDomain(task: Task): TaskEntity {
        return TaskEntity(
            id = task.id,
            title = task.title,
            description = task.description,
            startTimeEpoch = DateExt.toEpochMillis(task.startTime),
            endTimeEpoch = task.endTime?.let { DateExt.toEpochMillis(it) },
            isCompleted = task.isCompleted
        )
    }
}

