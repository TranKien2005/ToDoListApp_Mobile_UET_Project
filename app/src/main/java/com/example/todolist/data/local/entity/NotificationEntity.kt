package com.example.todolist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // NotificationType as String
    val relatedTaskId: Int?,
    val relatedMissionId: Int?,
    val title: String,
    val message: String,
    val scheduledTime: Long,
    val isDelivered: Boolean = false,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

