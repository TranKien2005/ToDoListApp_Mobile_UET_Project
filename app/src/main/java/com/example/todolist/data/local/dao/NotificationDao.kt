package com.example.todolist.data.local.dao

import androidx.room.*
import com.example.todolist.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications ORDER BY scheduledTime DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE isDelivered = 0 AND scheduledTime <= :currentTime ORDER BY scheduledTime ASC")
    suspend fun getPendingNotifications(currentTime: Long): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE id = :id")
    suspend fun getNotificationById(id: Long): NotificationEntity?

    @Query("SELECT * FROM notifications WHERE relatedTaskId = :taskId")
    suspend fun getNotificationsByTaskId(taskId: Int): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE relatedMissionId = :missionId")
    suspend fun getNotificationsByMissionId(missionId: Int): List<NotificationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isDelivered = 1 WHERE id = :id")
    suspend fun markAsDelivered(id: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: Long)

    @Query("DELETE FROM notifications WHERE relatedTaskId = :taskId")
    suspend fun deleteNotificationsByTaskId(taskId: Int)

    @Query("DELETE FROM notifications WHERE relatedMissionId = :missionId")
    suspend fun deleteNotificationsByMissionId(missionId: Int)

    @Query("DELETE FROM notifications WHERE isDelivered = 1 AND isRead = 1")
    suspend fun deleteReadNotifications()
}

