package com.example.todolist.domain.usecase

import com.example.todolist.core.model.Notification
import com.example.todolist.core.model.NotificationType
import com.example.todolist.core.model.Task
import com.example.todolist.core.model.Mission
import com.example.todolist.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow

// Get all notifications
interface GetNotificationsUseCase {
    operator fun invoke(): Flow<List<Notification>>
}

// Schedule notification for a task
interface ScheduleTaskNotificationUseCase {
    suspend operator fun invoke(task: Task, reminderMinutes: Int)
}

// Schedule notification for a mission
interface ScheduleMissionNotificationUseCase {
    suspend operator fun invoke(mission: Mission, warningMinutes: Int)
}

// Cancel all notifications for a task
interface CancelTaskNotificationsUseCase {
    suspend operator fun invoke(taskId: Int)
}

// Cancel all notifications for a mission
interface CancelMissionNotificationsUseCase {
    suspend operator fun invoke(missionId: Int)
}

// Mark notification as read
interface MarkNotificationAsReadUseCase {
    suspend operator fun invoke(notificationId: Long)
}

// Delete read notifications
interface DeleteReadNotificationsUseCase {
    suspend operator fun invoke()
}

// Create notification manually
interface CreateNotificationUseCase {
    suspend operator fun invoke(notification: Notification): Long
}

// Aggregator contains only the interfaces
data class NotificationUseCases(
    val getNotifications: GetNotificationsUseCase,
    val scheduleTaskNotification: ScheduleTaskNotificationUseCase,
    val scheduleMissionNotification: ScheduleMissionNotificationUseCase,
    val cancelTaskNotifications: CancelTaskNotificationsUseCase,
    val cancelMissionNotifications: CancelMissionNotificationsUseCase,
    val markNotificationAsRead: MarkNotificationAsReadUseCase,
    val deleteReadNotifications: DeleteReadNotificationsUseCase,
    val createNotification: CreateNotificationUseCase
)

