package com.example.todolist.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.todolist.data.local.database.AppDatabase
import com.example.todolist.data.repository.RoomNotificationRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver để xử lý sự kiện khởi động lại thiết bị
 * Khởi động lại tất cả notifications đã được schedule
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device booted, rescheduling notifications")

            // Start foreground service TRƯỚC KHI chạy background task
            // Phải start từ main thread, không phải từ coroutine
            try {
                NotificationForegroundService.start(context)
                Log.d(TAG, "Foreground service started")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting foreground service", e)
            }

            // Sử dụng goAsync để có thể chạy coroutine
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    rescheduleNotifications(context)
                } catch (e: Exception) {
                    Log.e(TAG, "Error rescheduling notifications", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private suspend fun rescheduleNotifications(context: Context) {
        val database = AppDatabase.getInstance(context)
        val repository = RoomNotificationRepositoryImpl(database.notificationDao())
        val scheduler = NotificationScheduler(context)

        val currentTime = System.currentTimeMillis()

        // Lấy tất cả notifications chưa được gửi
        val notifications = repository.getPendingNotifications(currentTime)

        Log.d(TAG, "Found ${notifications.size} pending notifications to reschedule")

        // Để tránh bị Android mute vì gửi quá nhiều notification cùng lúc,
        // thêm delay tăng dần cho mỗi notification quá hạn
        var delayOffset = 0L

        // Reschedule từng notification
        notifications.forEach { notification ->
            try {
                when {
                    notification.relatedTaskId != null -> {
                        scheduler.scheduleTaskNotification(
                            notification.id,
                            notification.scheduledTime,
                            additionalDelay = delayOffset
                        )
                    }
                    notification.relatedMissionId != null -> {
                        scheduler.scheduleMissionNotification(
                            notification.id,
                            notification.scheduledTime,
                            additionalDelay = delayOffset
                        )
                    }
                }
                Log.d(TAG, "Rescheduled notification ${notification.id} with delay offset ${delayOffset}ms")

                // Tăng delay 2 giây cho mỗi notification tiếp theo
                // để tránh spam
                delayOffset += 2000L
            } catch (e: Exception) {
                Log.e(TAG, "Error rescheduling notification ${notification.id}", e)
            }
        }
    }
}
