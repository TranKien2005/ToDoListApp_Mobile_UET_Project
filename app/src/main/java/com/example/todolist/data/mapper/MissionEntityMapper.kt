package com.example.todolist.data.mapper

import com.example.todolist.data.local.entity.MissionEntity
import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.MissionStoredStatus
import com.example.todolist.util.extension.DateExt

object MissionEntityMapper {
    fun toDomain(entity: MissionEntity): Mission {
        return Mission(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            deadline = DateExt.toLocalDateTime(entity.deadlineEpoch),
            storedStatus = try {
                MissionStoredStatus.valueOf(entity.status)
            } catch (_: Exception) {
                MissionStoredStatus.UNSPECIFIED
            }
        )
    }

    fun fromDomain(mission: Mission): MissionEntity {
        return MissionEntity(
            id = mission.id,
            title = mission.title,
            description = mission.description,
            deadlineEpoch = DateExt.toEpochMillis(mission.deadline),
            status = mission.storedStatus.name // Only store UNSPECIFIED or COMPLETED
        )
    }
}
