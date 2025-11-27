package com.example.todolist.data.mapper

import com.example.todolist.data.local.entity.MissionEntity
import com.example.todolist.domain.model.Mission
import com.example.todolist.util.extension.DateExt

object MissionEntityMapper {
    fun toDomain(entity: MissionEntity): Mission {
        return Mission(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            deadline = DateExt.toLocalDateTime(entity.deadlineEpoch),
            isCompleted = entity.isCompleted
        )
    }

    fun fromDomain(mission: Mission): MissionEntity {
        return MissionEntity(
            id = mission.id,
            title = mission.title,
            description = mission.description,
            deadlineEpoch = DateExt.toEpochMillis(mission.deadline),
            isCompleted = mission.isCompleted
        )
    }
}

