package com.example.todolist.notification.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todolist.core.model.Notification
import com.example.todolist.core.model.NotificationType
import com.example.todolist.data.local.database.AppDatabase
import com.example.todolist.data.local.entity.MissionEntity
import com.example.todolist.data.repository.RoomNotificationRepositoryImpl
import com.example.todolist.data.repository.RoomSettingsRepositoryImpl
import com.example.todolist.notification.NotificationHelper
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Worker để gửi thông báo tóm tắt missions hàng ngày
 */
class DailyMissionSummaryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getInstance(applicationContext)
            val settingsRepo = RoomSettingsRepositoryImpl(database.settingsDao())
            val settings = settingsRepo.getSettings().first()

            // Kiểm tra nếu tắt thông báo daily
            if (!settings.notifyDailyMissions) {
                return Result.success()
            }

            val missionDao = database.missionDao()
            val notificationRepo = RoomNotificationRepositoryImpl(database.notificationDao())

            // Lấy missions có deadline trong ngày hôm nay
            val now = LocalDateTime.now()
            val startOfDay = now.truncatedTo(ChronoUnit.DAYS)
            val endOfDay = startOfDay.plusDays(1)

            val todayMissions = missionDao.getAll()
                .first()
                .filter { entity ->
                    val deadline = Instant.ofEpochMilli(entity.deadlineEpoch)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                    deadline.isAfter(startOfDay) && deadline.isBefore(endOfDay) &&
                            entity.status == "UNSPECIFIED"
                }

            if (todayMissions.isNotEmpty()) {
                val message = buildDailySummaryMessage(todayMissions)
                val title = "Missions hôm nay (${todayMissions.size})"

                // Tạo notification trong DB
                val notification = Notification(
                    type = NotificationType.MISSION_DAILY_SUMMARY,
                    title = title,
                    message = message,
                    scheduledTime = System.currentTimeMillis(),
                    isDelivered = true
                )
                val notificationId = notificationRepo.insertNotification(notification)

                // Hiển thị thông báo ngay
                val notificationHelper = NotificationHelper(applicationContext)
                notificationHelper.showNotification(
                    notificationId = notificationId,
                    notificationType = NotificationType.MISSION_DAILY_SUMMARY,
                    title = title,
                    message = message
                )
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun buildDailySummaryMessage(missions: List<MissionEntity>): String {
        return missions.take(5).joinToString("\n") { "• ${it.title}" } +
                if (missions.size > 5) "\n... và ${missions.size - 5} mission khác" else ""
    }
}
