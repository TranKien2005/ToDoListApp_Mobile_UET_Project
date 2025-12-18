package com.example.todolist.data.mapper

import com.example.todolist.core.model.Notification
import com.example.todolist.core.model.NotificationType
import com.example.todolist.data.local.entity.NotificationEntity
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for NotificationEntityMapper
 */
class NotificationEntityMapperTest {

    @Test
    fun `fromDomain converts Notification to NotificationEntity`() {
        val now = System.currentTimeMillis()
        val notification = Notification(
            id = 1,
            type = NotificationType.TASK_REMINDER,
            relatedTaskId = 100,
            relatedMissionId = null,
            title = "Reminder",
            message = "Task starts soon",
            scheduledTime = now,
            isDelivered = true,
            isRead = false,
            createdAt = now
        )

        val entity = NotificationEntityMapper.fromDomain(notification)

        assertEquals(1L, entity.id)
        assertEquals("TASK_REMINDER", entity.type)
        assertEquals(100, entity.relatedTaskId)
        assertNull(entity.relatedMissionId)
        assertEquals("Reminder", entity.title)
        assertTrue(entity.isDelivered)
        assertFalse(entity.isRead)
    }

    @Test
    fun `toDomain converts NotificationEntity to Notification`() {
        val now = System.currentTimeMillis()
        val entity = NotificationEntity(
            id = 2,
            type = "MISSION_DEADLINE_WARNING",
            relatedTaskId = null,
            relatedMissionId = 50,
            title = "Warning",
            message = "Mission deadline approaching",
            scheduledTime = now,
            isDelivered = false,
            isRead = true,
            createdAt = now
        )

        val notification = NotificationEntityMapper.toDomain(entity)

        assertEquals(2L, notification.id)
        assertEquals(NotificationType.MISSION_DEADLINE_WARNING, notification.type)
        assertNull(notification.relatedTaskId)
        assertEquals(50, notification.relatedMissionId)
        assertEquals("Warning", notification.title)
        assertFalse(notification.isDelivered)
        assertTrue(notification.isRead)
    }

    @Test
    fun `round trip conversion preserves data`() {
        val now = System.currentTimeMillis()
        val original = Notification(
            id = 5,
            type = NotificationType.TASK_OVERDUE,
            relatedTaskId = 25,
            relatedMissionId = null,
            title = "Overdue",
            message = "Task is overdue",
            scheduledTime = now,
            isDelivered = true,
            isRead = true,
            createdAt = now
        )

        val entity = NotificationEntityMapper.fromDomain(original)
        val converted = NotificationEntityMapper.toDomain(entity)

        assertEquals(original.id, converted.id)
        assertEquals(original.type, converted.type)
        assertEquals(original.relatedTaskId, converted.relatedTaskId)
        assertEquals(original.title, converted.title)
        assertEquals(original.isDelivered, converted.isDelivered)
        assertEquals(original.isRead, converted.isRead)
    }

    @Test
    fun `all notification types can be converted`() {
        val now = System.currentTimeMillis()
        NotificationType.values().forEach { type ->
            val notification = Notification(
                type = type,
                title = "Test",
                message = "Message",
                scheduledTime = now,
                createdAt = now
            )
            val entity = NotificationEntityMapper.fromDomain(notification)
            assertEquals(type.name, entity.type)
        }
    }
}
