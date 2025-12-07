package com.example.todolist.domain.usecase

import android.content.Context
import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.Notification
import com.example.todolist.core.model.NotificationType
import com.example.todolist.core.model.Task
import com.example.todolist.domain.repository.NotificationRepository
import com.example.todolist.notification.NotificationScheduler
import com.example.todolist.R
import kotlinx.coroutines.flow.Flow
import java.time.ZoneId

// Implementation for GetNotificationsUseCase
class RealGetNotificationsUseCase(
    private val repository: NotificationRepository
) : GetNotificationsUseCase {
    override fun invoke(): Flow<List<Notification>> {
        return repository.getAllNotifications()
    }
}

// Implementation for ScheduleTaskNotificationUseCase
class RealScheduleTaskNotificationUseCase(
    private val repository: NotificationRepository,
    private val scheduler: NotificationScheduler,
    private val context: Context
) : ScheduleTaskNotificationUseCase {
    override suspend fun invoke(task: Task, reminderMinutes: Int) {
        // Tính toán thời gian thông báo = startTime - reminderMinutes
        val notificationTime = task.startTime.minusMinutes(reminderMinutes.toLong())
        val scheduledTimeMillis = notificationTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // Không tạo notification nếu đã quá thời gian
        if (scheduledTimeMillis < System.currentTimeMillis()) {
            return
        }

        // Tạo notification record
        val notification = Notification(
            type = NotificationType.TASK_REMINDER,
            relatedTaskId = task.id,
            title = context.getString(R.string.notification_task_reminder_title, task.title),
            message = context.getString(R.string.notification_task_reminder_message, reminderMinutes),
            scheduledTime = scheduledTimeMillis
        )

        val notificationId = repository.insertNotification(notification)

        // Lên lịch với WorkManager
        scheduler.scheduleTaskNotification(notificationId, scheduledTimeMillis)
    }
}

// Implementation for ScheduleMissionNotificationUseCase
class RealScheduleMissionNotificationUseCase(
    private val repository: NotificationRepository,
    private val scheduler: NotificationScheduler,
    private val context: Context
) : ScheduleMissionNotificationUseCase {
    override suspend fun invoke(mission: Mission, warningMinutes: Int) {
        val now = System.currentTimeMillis()

        // 1. TẠO NOTIFICATION CẢNH BÁO TRƯỚC DEADLINE
        val warningTime = mission.deadline.minusMinutes(warningMinutes.toLong())
        val warningTimeMillis = warningTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (warningTimeMillis > now) {
            val warningNotification = Notification(
                type = NotificationType.MISSION_DEADLINE_WARNING,
                relatedMissionId = mission.id,
                title = context.getString(R.string.notification_mission_warning_title, mission.title),
                message = context.getString(R.string.notification_mission_warning_message, warningMinutes),
                scheduledTime = warningTimeMillis
            )

            val warningNotifId = repository.insertNotification(warningNotification)
            scheduler.scheduleMissionNotification(warningNotifId, warningTimeMillis)
        }

        // 2. TẠO NOTIFICATION OVERDUE (khi qua deadline)
        val deadlineMillis = mission.deadline.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (deadlineMillis > now) {
            val overdueNotification = Notification(
                type = NotificationType.MISSION_OVERDUE,
                relatedMissionId = mission.id,
                title = context.getString(R.string.notification_mission_overdue_title, mission.title),
                message = context.getString(R.string.notification_mission_overdue_message),
                scheduledTime = deadlineMillis // Gửi ngay khi deadline
            )

            val overdueNotifId = repository.insertNotification(overdueNotification)
            scheduler.scheduleMissionNotification(overdueNotifId, deadlineMillis)
        }
    }
}

// Implementation for CancelTaskNotificationsUseCase
class RealCancelTaskNotificationsUseCase(
    private val repository: NotificationRepository,
    private val scheduler: NotificationScheduler
) : CancelTaskNotificationsUseCase {
    override suspend fun invoke(taskId: Int) {
        // Lấy tất cả notifications của task
        val notifications = repository.getNotificationsByTaskId(taskId)

        // Hủy từng notification
        notifications.forEach { notification ->
            scheduler.cancelNotification(notification.id, isTask = true)
        }

        // Xóa khỏi database
        repository.deleteNotificationsByTaskId(taskId)
    }
}

// Implementation for CancelMissionNotificationsUseCase
class RealCancelMissionNotificationsUseCase(
    private val repository: NotificationRepository,
    private val scheduler: NotificationScheduler
) : CancelMissionNotificationsUseCase {
    override suspend fun invoke(missionId: Int) {
        // Lấy tất cả notifications của mission
        val notifications = repository.getNotificationsByMissionId(missionId)

        // Hủy từng notification
        notifications.forEach { notification ->
            scheduler.cancelNotification(notification.id, isTask = false)
        }

        // Xóa khỏi database
        repository.deleteNotificationsByMissionId(missionId)
    }
}

// Implementation for MarkNotificationAsReadUseCase
class RealMarkNotificationAsReadUseCase(
    private val repository: NotificationRepository
) : MarkNotificationAsReadUseCase {
    override suspend fun invoke(notificationId: Long) {
        repository.markAsRead(notificationId)
    }
}

// Implementation for DeleteReadNotificationsUseCase
class RealDeleteReadNotificationsUseCase(
    private val repository: NotificationRepository
) : DeleteReadNotificationsUseCase {
    override suspend fun invoke() {
        repository.deleteReadNotifications()
    }
}

// Implementation for CreateNotificationUseCase
class RealCreateNotificationUseCase(
    private val repository: NotificationRepository
) : CreateNotificationUseCase {
    override suspend fun invoke(notification: Notification): Long {
        return repository.insertNotification(notification)
    }
}
