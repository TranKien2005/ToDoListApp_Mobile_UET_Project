package com.example.todolist.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.todolist.R

/**
 * Foreground Service để đảm bảo notification system luôn hoạt động
 * ngay cả khi app bị đóng
 */
class NotificationForegroundService : Service() {

    companion object {
        private const val CHANNEL_ID = "notification_service_channel"
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, NotificationForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, NotificationForegroundService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("NotificationForegroundService", "onCreate called")
        try {
            createNotificationChannel()
            Log.d("NotificationForegroundService", "Notification channel created")
            val notification = createNotification()
            Log.d("NotificationForegroundService", "Notification created")
            startForeground(NOTIFICATION_ID, notification)
            Log.d("NotificationForegroundService", "Started foreground successfully")
        } catch (e: Exception) {
            Log.e("NotificationForegroundService", "Error in onCreate", e)
            throw e
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY // Service sẽ được restart nếu bị kill
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notification Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps notification system running in background"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Task Reminders Active")
            .setContentText("Notification system is running")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
    }
}
