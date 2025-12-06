package com.example.todolist.data.repository

import com.example.todolist.core.model.Notification
import com.example.todolist.data.local.dao.NotificationDao
import com.example.todolist.data.mapper.NotificationEntityMapper
import com.example.todolist.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomNotificationRepositoryImpl(
    private val notificationDao: NotificationDao
) : NotificationRepository {

    override fun getAllNotifications(): Flow<List<Notification>> {
        return notificationDao.getAllNotifications().map { entities ->
            entities.map { NotificationEntityMapper.toDomain(it) }
        }
    }

    override suspend fun getPendingNotifications(currentTime: Long): List<Notification> {
        return notificationDao.getPendingNotifications(currentTime)
            .map { NotificationEntityMapper.toDomain(it) }
    }

    override suspend fun getNotificationById(id: Long): Notification? {
        return notificationDao.getNotificationById(id)?.let {
            NotificationEntityMapper.toDomain(it)
        }
    }

    override suspend fun getNotificationsByTaskId(taskId: Int): List<Notification> {
        return notificationDao.getNotificationsByTaskId(taskId)
            .map { NotificationEntityMapper.toDomain(it) }
    }

    override suspend fun getNotificationsByMissionId(missionId: Int): List<Notification> {
        return notificationDao.getNotificationsByMissionId(missionId)
            .map { NotificationEntityMapper.toDomain(it) }
    }

    override suspend fun insertNotification(notification: Notification): Long {
        val entity = NotificationEntityMapper.fromDomain(notification)
        return notificationDao.insertNotification(entity)
    }

    override suspend fun insertNotifications(notifications: List<Notification>) {
        val entities = notifications.map { NotificationEntityMapper.fromDomain(it) }
        notificationDao.insertNotifications(entities)
    }

    override suspend fun updateNotification(notification: Notification) {
        val entity = NotificationEntityMapper.fromDomain(notification)
        notificationDao.updateNotification(entity)
    }

    override suspend fun markAsDelivered(id: Long) {
        notificationDao.markAsDelivered(id)
    }

    override suspend fun markAsRead(id: Long) {
        notificationDao.markAsRead(id)
    }

    override suspend fun deleteNotification(id: Long) {
        notificationDao.deleteNotification(id)
    }

    override suspend fun deleteNotificationsByTaskId(taskId: Int) {
        notificationDao.deleteNotificationsByTaskId(taskId)
    }

    override suspend fun deleteNotificationsByMissionId(missionId: Int) {
        notificationDao.deleteNotificationsByMissionId(missionId)
    }

    override suspend fun deleteReadNotifications() {
        notificationDao.deleteReadNotifications()
    }
}

