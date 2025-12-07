package com.example.todolist.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.todolist.MainActivity
import com.example.todolist.R
import com.example.todolist.core.model.NotificationType

/**
 * Helper class để tạo và quản lý Android notifications
 */
class NotificationHelper(private val context: Context) {

    companion object {
        private const val TAG = "NotificationHelper"
        // Notification Channels
        const val CHANNEL_TASK_REMINDER = "channel_task_reminder"
        const val CHANNEL_MISSION_SUMMARY = "channel_mission_summary"
        const val CHANNEL_MISSION_WARNING = "channel_mission_warning"
        const val CHANNEL_OVERDUE = "channel_overdue"

        private const val GROUP_KEY_TASKS = "com.example.todolist.TASKS"
        private const val GROUP_KEY_MISSIONS = "com.example.todolist.MISSIONS"
    }

    init {
        createNotificationChannels()
    }

    /**
     * Tạo các notification channels (required cho Android 8.0+)
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "Creating notification channels...")
            try {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                // Channel cho Task Reminder
                val taskReminderChannel = NotificationChannel(
                    CHANNEL_TASK_REMINDER,
                    context.getString(R.string.channel_task_reminder_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.channel_task_reminder_desc)
                    enableVibration(true)
                }
                Log.d(TAG, "Task reminder channel created")

                // Channel cho Mission Summary
                val missionSummaryChannel = NotificationChannel(
                    CHANNEL_MISSION_SUMMARY,
                    context.getString(R.string.channel_mission_summary_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.channel_mission_summary_desc)
                }
                Log.d(TAG, "Mission summary channel created")

                // Channel cho Mission Warning
                val missionWarningChannel = NotificationChannel(
                    CHANNEL_MISSION_WARNING,
                    context.getString(R.string.channel_mission_warning_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.channel_mission_warning_desc)
                    enableVibration(true)
                }
                Log.d(TAG, "Mission warning channel created")

                // Channel cho Overdue
                val overdueChannel = NotificationChannel(
                    CHANNEL_OVERDUE,
                    context.getString(R.string.channel_overdue_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.channel_overdue_desc)
                    enableVibration(true)
                }
                Log.d(TAG, "Overdue channel created")

                notificationManager.createNotificationChannels(
                    listOf(
                        taskReminderChannel,
                        missionSummaryChannel,
                        missionWarningChannel,
                        overdueChannel
                    )
                )
                Log.d(TAG, "All notification channels registered successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating notification channels", e)
                throw e
            }
        }
    }

    /**
     * Hiển thị notification
     */
    fun showNotification(
        notificationId: Long,
        notificationType: NotificationType,
        title: String,
        message: String,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT
    ) {
        Log.d(TAG, "showNotification called: id=$notificationId, type=$notificationType, title=$title")

        // Kiểm tra permission trên Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                Log.e(TAG, "POST_NOTIFICATIONS permission not granted!")
                return
            }
        }

        val channelId = getChannelIdForType(notificationType)
        val groupKey = getGroupKeyForType(notificationType)

        Log.d(TAG, "Creating notification with channel=$channelId, group=$groupKey")

        // Intent để mở app khi click vào notification
        // Thêm notification ID vào intent để MainActivity biết cần mark as read
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_id", notificationId)
            putExtra("mark_as_read", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setGroup(groupKey)
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId.toInt(), notification)
            Log.d(TAG, "Notification shown successfully: $notificationId")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification", e)
        }
    }

    /**
     * Hủy notification
     */
    fun cancelNotification(notificationId: Long) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(notificationId.toInt())
    }

    /**
     * Lấy channel ID dựa trên loại notification
     */
    private fun getChannelIdForType(type: NotificationType): String {
        return when (type) {
            NotificationType.TASK_REMINDER -> CHANNEL_TASK_REMINDER
            NotificationType.MISSION_DEADLINE_WARNING -> CHANNEL_MISSION_WARNING
            NotificationType.MISSION_DAILY_SUMMARY,
            NotificationType.MISSION_WEEKLY_SUMMARY,
            NotificationType.MISSION_MONTHLY_SUMMARY -> CHANNEL_MISSION_SUMMARY
            NotificationType.TASK_OVERDUE,
            NotificationType.MISSION_OVERDUE -> CHANNEL_OVERDUE
        }
    }

    /**
     * Lấy group key dựa trên loại notification
     */
    private fun getGroupKeyForType(type: NotificationType): String {
        return when (type) {
            NotificationType.TASK_REMINDER,
            NotificationType.TASK_OVERDUE -> GROUP_KEY_TASKS
            else -> GROUP_KEY_MISSIONS
        }
    }
}
