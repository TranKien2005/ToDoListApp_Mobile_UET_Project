package com.example.todolist.data.local.entity

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for NotificationEntity
 */
class NotificationEntityTest {

    @Test
    fun `create notification entity with all fields`() {
        val now = System.currentTimeMillis()
        val entity = NotificationEntity(
            id = 1,
            type = "TASK_REMINDER",
            relatedTaskId = 100,
            relatedMissionId = null,
            title = "Test Notification",
            message = "Test message",
            scheduledTime = now,
            isDelivered = true,
            isRead = false,
            createdAt = now
        )

        assertEquals(1L, entity.id)
        assertEquals("TASK_REMINDER", entity.type)
        assertEquals(100, entity.relatedTaskId)
        assertNull(entity.relatedMissionId)
        assertTrue(entity.isDelivered)
        assertFalse(entity.isRead)
    }

    @Test
    fun `create notification entity with default values`() {
        val now = System.currentTimeMillis()
        val entity = NotificationEntity(
            type = "TASK_REMINDER",
            relatedTaskId = null,
            relatedMissionId = null,
            title = "Simple",
            message = "",
            scheduledTime = now
        )

        assertEquals(0L, entity.id)
        assertNull(entity.relatedTaskId)
        assertNull(entity.relatedMissionId)
        assertFalse(entity.isDelivered)
        assertFalse(entity.isRead)
    }

    @Test
    fun `notification entity equality`() {
        val now = System.currentTimeMillis()
        val entity1 = NotificationEntity(id = 1, type = "TASK_REMINDER", relatedTaskId = null, relatedMissionId = null, title = "Test", message = "", scheduledTime = now, createdAt = now)
        val entity2 = NotificationEntity(id = 1, type = "TASK_REMINDER", relatedTaskId = null, relatedMissionId = null, title = "Test", message = "", scheduledTime = now, createdAt = now)
        
        assertEquals(entity1, entity2)
    }
}
