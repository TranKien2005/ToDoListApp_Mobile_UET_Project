package com.example.todolist.notification.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todolist.data.local.database.AppDatabase
import com.example.todolist.data.repository.RoomNotificationRepositoryImpl
import com.example.todolist.notification.NotificationHelper

/**
 * Worker để gửi các notification đã được scheduled
 */
class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val notificationId = inputData.getLong("notification_id", -1L)
            if (notificationId == -1L) {
                return Result.failure()
            }

            // Get notification from database
            val database = AppDatabase.getInstance(applicationContext)
            val repository = RoomNotificationRepositoryImpl(database.notificationDao())
            val notification = repository.getNotificationById(notificationId)

            if (notification != null && !notification.isDelivered) {
                // Show Android notification
                val notificationHelper = NotificationHelper(applicationContext)
                notificationHelper.showNotification(
                    notificationId = notification.id,
                    notificationType = notification.type,
                    title = notification.title,
                    message = notification.message
                )

                // Mark as delivered
                repository.markAsDelivered(notificationId)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}

