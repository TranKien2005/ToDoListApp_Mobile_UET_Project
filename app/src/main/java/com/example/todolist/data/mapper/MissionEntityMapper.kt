package com.example.todolist.data.mapper

import com.example.todolist.data.local.entity.MissionEntity
import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.MissionStatus
import com.example.todolist.util.extension.DateExt

object MissionEntityMapper {
    fun toDomain(entity: MissionEntity): Mission {
        return Mission(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            deadline = DateExt.toLocalDateTime(entity.deadlineEpoch),
            status = try { MissionStatus.valueOf(entity.status) } catch (_: Exception) { MissionStatus.UNSPECIFIED }
        )
    }

    fun fromDomain(mission: Mission): MissionEntity {
        return MissionEntity(
            id = mission.id,
            title = mission.title,
            description = mission.description,
            deadlineEpoch = DateExt.toEpochMillis(mission.deadline),
            status = mission.status.name
        )
    }
}
