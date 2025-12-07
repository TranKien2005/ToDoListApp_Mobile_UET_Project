package com.example.todolist.notification.workers

import android.content.Context
import android.util.Log
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

    companion object {
        private const val TAG = "NotificationWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            val notificationId = inputData.getLong("notification_id", -1L)
            Log.d(TAG, "doWork started for notification ID: $notificationId")

            if (notificationId == -1L) {
                Log.e(TAG, "Invalid notification ID")
                return Result.failure()
            }

            // Get notification from database
            val database = AppDatabase.getInstance(applicationContext)
            val repository = RoomNotificationRepositoryImpl(database.notificationDao())
            val notification = repository.getNotificationById(notificationId)

            Log.d(TAG, "Notification from DB: $notification")

            if (notification != null && !notification.isDelivered) {
                Log.d(TAG, "Showing notification: ${notification.title}")

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
                Log.d(TAG, "Notification marked as delivered")
            } else {
                Log.w(TAG, "Notification is null or already delivered")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in NotificationWorker", e)
            e.printStackTrace()
            Result.failure()
        }
    }
}
