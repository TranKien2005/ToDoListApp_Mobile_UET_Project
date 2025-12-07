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
import com.example.todolist.R
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.DayOfWeek

/**
 * Worker để gửi thông báo tóm tắt missions hàng tuần
 */
class WeeklyMissionSummaryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getInstance(applicationContext)
            val settingsRepo = RoomSettingsRepositoryImpl(database.settingsDao())
            val settings = settingsRepo.getSettings().first()

            // Kiểm tra nếu tắt thông báo weekly
            if (!settings.notifyWeeklyMissions) {
                return Result.success()
            }

            val missionDao = database.missionDao()
            val notificationRepo = RoomNotificationRepositoryImpl(database.notificationDao())

            // Lấy missions có deadline trong tuần này
            val now = LocalDateTime.now()
            val startOfWeek = now.with(DayOfWeek.MONDAY).truncatedTo(ChronoUnit.DAYS)
            val endOfWeek = startOfWeek.plusWeeks(1)

            val weekMissions = missionDao.getAll()
                .first()
                .filter { entity ->
                    val deadline = Instant.ofEpochMilli(entity.deadlineEpoch)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                    deadline.isAfter(startOfWeek) && deadline.isBefore(endOfWeek) &&
                            entity.status == "UNSPECIFIED"
                }

            if (weekMissions.isNotEmpty()) {
                val message = buildWeeklySummaryMessage(weekMissions)
                val title = applicationContext.getString(
                    R.string.notification_weekly_summary_title,
                    weekMissions.size
                )

                // Tạo notification trong DB
                val notification = Notification(
                    type = NotificationType.MISSION_WEEKLY_SUMMARY,
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
                    notificationType = NotificationType.MISSION_WEEKLY_SUMMARY,
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

    private fun buildWeeklySummaryMessage(missions: List<MissionEntity>): String {
        val dateTimeFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        return missions.take(5).joinToString("\n") {
            val deadline = Instant.ofEpochMilli(it.deadlineEpoch)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(dateTimeFormatter)
            "${it.title}, $deadline"
        } + if (missions.size > 5) "\n${applicationContext.getString(R.string.notification_summary_and_more, missions.size - 5)}" else ""
    }
}
