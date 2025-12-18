package com.example.todolist.feature.notification

import com.example.todolist.core.model.Notification
import com.example.todolist.core.model.NotificationType
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for NotificationUiState-related logic
 */
class NotificationUiStateTest {

    @Test
    fun `empty notification list`() {
        val notifications = emptyList<Notification>()
        assertTrue(notifications.isEmpty())
        assertEquals(0, notifications.size)
    }

    @Test
    fun `unread count calculation`() {
        val notifications = listOf(
            Notification(id = 1, type = NotificationType.TASK_REMINDER, title = "1", message = "", scheduledTime = 1000, isRead = false),
            Notification(id = 2, type = NotificationType.TASK_REMINDER, title = "2", message = "", scheduledTime = 2000, isRead = true),
            Notification(id = 3, type = NotificationType.TASK_REMINDER, title = "3", message = "", scheduledTime = 3000, isRead = false)
        )

        val unreadCount = notifications.count { !it.isRead }
        assertEquals(2, unreadCount)
    }

    @Test
    fun `has unread notifications`() {
        val notifications = listOf(
            Notification(id = 1, type = NotificationType.TASK_REMINDER, title = "1", message = "", scheduledTime = 1000, isRead = false)
        )

        val hasUnread = notifications.any { !it.isRead }
        assertTrue(hasUnread)
    }

    @Test
    fun `no unread notifications`() {
        val notifications = listOf(
            Notification(id = 1, type = NotificationType.TASK_REMINDER, title = "1", message = "", scheduledTime = 1000, isRead = true)
        )

        val hasUnread = notifications.any { !it.isRead }
        assertFalse(hasUnread)
    }

    @Test
    fun `sort notifications by scheduled time`() {
        val notifications = listOf(
            Notification(id = 1, type = NotificationType.TASK_REMINDER, title = "1", message = "", scheduledTime = 1000),
            Notification(id = 2, type = NotificationType.TASK_REMINDER, title = "2", message = "", scheduledTime = 3000),
            Notification(id = 3, type = NotificationType.TASK_REMINDER, title = "3", message = "", scheduledTime = 2000)
        )

        val sorted = notifications.sortedByDescending { it.scheduledTime }
        assertEquals(2L, sorted[0].id)
        assertEquals(3L, sorted[1].id)
        assertEquals(1L, sorted[2].id)
    }

    @Test
    fun `filter by type`() {
        val notifications = listOf(
            Notification(id = 1, type = NotificationType.TASK_REMINDER, title = "Task", message = "", scheduledTime = 1000),
            Notification(id = 2, type = NotificationType.MISSION_OVERDUE, title = "Mission", message = "", scheduledTime = 2000)
        )

        val taskOnly = notifications.filter { it.type == NotificationType.TASK_REMINDER }
        assertEquals(1, taskOnly.size)
    }
}
