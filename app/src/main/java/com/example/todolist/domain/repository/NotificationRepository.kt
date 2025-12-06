package com.example.todolist.domain.repository

import com.example.todolist.core.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getAllNotifications(): Flow<List<Notification>>
    suspend fun getPendingNotifications(currentTime: Long): List<Notification>
    suspend fun getNotificationById(id: Long): Notification?
    suspend fun getNotificationsByTaskId(taskId: Int): List<Notification>
    suspend fun getNotificationsByMissionId(missionId: Int): List<Notification>
    suspend fun insertNotification(notification: Notification): Long
    suspend fun insertNotifications(notifications: List<Notification>)
    suspend fun updateNotification(notification: Notification)
    suspend fun markAsDelivered(id: Long)
    suspend fun markAsRead(id: Long)
    suspend fun deleteNotification(id: Long)
    suspend fun deleteNotificationsByTaskId(taskId: Int)
    suspend fun deleteNotificationsByMissionId(missionId: Int)
    suspend fun deleteReadNotifications()
}

