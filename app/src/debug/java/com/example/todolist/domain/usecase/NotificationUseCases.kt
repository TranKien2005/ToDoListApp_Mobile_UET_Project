package com.example.todolist.domain.usecase

import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.Notification
import com.example.todolist.core.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// Debug/Mock implementations for testing

class DebugGetNotificationsUseCase : GetNotificationsUseCase {
    override fun invoke(): Flow<List<Notification>> {
        return flowOf(emptyList())
    }
}

class DebugScheduleTaskNotificationUseCase : ScheduleTaskNotificationUseCase {
    override suspend fun invoke(task: Task, reminderMinutes: Int) {
        // Mock implementation
        println("Debug: Scheduled task notification for ${task.title}")
    }
}

class DebugScheduleMissionNotificationUseCase : ScheduleMissionNotificationUseCase {
    override suspend fun invoke(mission: Mission, warningMinutes: Int) {
        // Mock implementation
        println("Debug: Scheduled mission notification for ${mission.title}")
    }
}

class DebugCancelTaskNotificationsUseCase : CancelTaskNotificationsUseCase {
    override suspend fun invoke(taskId: Int) {
        // Mock implementation
        println("Debug: Cancelled task notifications for task $taskId")
    }
}

class DebugCancelMissionNotificationsUseCase : CancelMissionNotificationsUseCase {
    override suspend fun invoke(missionId: Int) {
        // Mock implementation
        println("Debug: Cancelled mission notifications for mission $missionId")
    }
}

class DebugMarkNotificationAsReadUseCase : MarkNotificationAsReadUseCase {
    override suspend fun invoke(notificationId: Long) {
        // Mock implementation
        println("Debug: Marked notification $notificationId as read")
    }
}

class DebugDeleteReadNotificationsUseCase : DeleteReadNotificationsUseCase {
    override suspend fun invoke() {
        // Mock implementation
        println("Debug: Deleted read notifications")
    }
}

class DebugCreateNotificationUseCase : CreateNotificationUseCase {
    override suspend fun invoke(notification: Notification): Long {
        // Mock implementation
        println("Debug: Created notification ${notification.title}")
        return 1L
    }
}

val fakeNotificationUseCases = NotificationUseCases(
    getNotifications = DebugGetNotificationsUseCase(),
    scheduleTaskNotification = DebugScheduleTaskNotificationUseCase(),
    scheduleMissionNotification = DebugScheduleMissionNotificationUseCase(),
    cancelTaskNotifications = DebugCancelTaskNotificationsUseCase(),
    cancelMissionNotifications = DebugCancelMissionNotificationsUseCase(),
    markNotificationAsRead = DebugMarkNotificationAsReadUseCase(),
    deleteReadNotifications = DebugDeleteReadNotificationsUseCase(),
    createNotification = DebugCreateNotificationUseCase()
)
