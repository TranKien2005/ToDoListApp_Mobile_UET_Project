package com.example.todolist.core.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Notification model
 */
class NotificationTest {

    @Test
    fun `create notification with all fields`() {
        val notification = Notification(
            id = 1,
            type = NotificationType.TASK_REMINDER,
            relatedTaskId = 100,
            relatedMissionId = null,
            title = "Test Notification",
            message = "This is a test message",
            scheduledTime = System.currentTimeMillis(),
            isDelivered = true,
            isRead = false
        )

        assertEquals(1L, notification.id)
        assertEquals(NotificationType.TASK_REMINDER, notification.type)
        assertEquals(100, notification.relatedTaskId)
        assertNull(notification.relatedMissionId)
        assertEquals("Test Notification", notification.title)
        assertEquals("This is a test message", notification.message)
        assertTrue(notification.isDelivered)
        assertFalse(notification.isRead)
    }

    @Test
    fun `notification type enum has all values`() {
        val types = NotificationType.values()
        assertEquals(7, types.size)
        assertTrue(types.contains(NotificationType.TASK_REMINDER))
        assertTrue(types.contains(NotificationType.TASK_OVERDUE))
        assertTrue(types.contains(NotificationType.MISSION_DEADLINE_WARNING))
        assertTrue(types.contains(NotificationType.MISSION_DAILY_SUMMARY))
        assertTrue(types.contains(NotificationType.MISSION_WEEKLY_SUMMARY))
        assertTrue(types.contains(NotificationType.MISSION_MONTHLY_SUMMARY))
        assertTrue(types.contains(NotificationType.MISSION_OVERDUE))
    }

    @Test
    fun `notification default values`() {
        val notification = Notification(
            type = NotificationType.TASK_REMINDER,
            title = "Simple",
            message = "",
            scheduledTime = System.currentTimeMillis()
        )

        assertEquals(0L, notification.id)
        assertFalse(notification.isDelivered)
        assertFalse(notification.isRead)
        assertNull(notification.relatedTaskId)
        assertNull(notification.relatedMissionId)
    }

    @Test
    fun `notification can be marked as read`() {
        val notification = Notification(
            type = NotificationType.TASK_REMINDER,
            title = "Test",
            message = "Message",
            scheduledTime = System.currentTimeMillis(),
            isRead = false
        )

        val readNotification = notification.copy(isRead = true)
        assertTrue(readNotification.isRead)
    }
}
