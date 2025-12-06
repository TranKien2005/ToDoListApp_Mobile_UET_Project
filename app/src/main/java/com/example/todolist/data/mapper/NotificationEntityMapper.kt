package com.example.todolist.data.mapper

import com.example.todolist.core.model.Notification
import com.example.todolist.core.model.NotificationType
import com.example.todolist.data.local.entity.NotificationEntity

object NotificationEntityMapper {

    fun toDomain(entity: NotificationEntity): Notification {
        return Notification(
            id = entity.id,
            type = NotificationType.valueOf(entity.type),
            relatedTaskId = entity.relatedTaskId,
            relatedMissionId = entity.relatedMissionId,
            title = entity.title,
            message = entity.message,
            scheduledTime = entity.scheduledTime,
            isDelivered = entity.isDelivered,
            isRead = entity.isRead,
            createdAt = entity.createdAt
        )
    }

    fun fromDomain(notification: Notification): NotificationEntity {
        return NotificationEntity(
            id = notification.id,
            type = notification.type.name,
            relatedTaskId = notification.relatedTaskId,
            relatedMissionId = notification.relatedMissionId,
            title = notification.title,
            message = notification.message,
            scheduledTime = notification.scheduledTime,
            isDelivered = notification.isDelivered,
            isRead = notification.isRead,
            createdAt = notification.createdAt
        )
    }
}

