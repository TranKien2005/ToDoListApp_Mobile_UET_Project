package com.example.todolist.data.mapper

import com.example.todolist.core.model.Notification
import com.example.todolist.core.model.NotificationType
import com.example.todolist.data.local.entity.NotificationEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationEntityMapperTest {

    @Test
    fun `toDomain maps entity to notification correctly`() {
        val entity = NotificationEntity(
            id = 1L,
            type = "TASK_REMINDER",
            relatedTaskId = 42,
            relatedMissionId = null,
            title = "Task Reminder",
            message = "Don't forget your task!",
            scheduledTime = 1702900800000L,
            isDelivered = true,
            isRead = false,
            createdAt = 1702800000000L
        )
        
        val notification = NotificationEntityMapper.toDomain(entity)
        
        assertEquals(1L, notification.id)
        assertEquals(NotificationType.TASK_REMINDER, notification.type)
        assertEquals(42, notification.relatedTaskId)
        assertNull(notification.relatedMissionId)
        assertEquals("Task Reminder", notification.title)
        assertEquals("Don't forget your task!", notification.message)
        assertEquals(1702900800000L, notification.scheduledTime)
        assertTrue(notification.isDelivered)
        assertEquals(false, notification.isRead)
        assertEquals(1702800000000L, notification.createdAt)
    }

    @Test
    fun `fromDomain maps notification to entity correctly`() {
        val notification = Notification(
            id = 1L,
            type = NotificationType.MISSION_DEADLINE_WARNING,
            relatedTaskId = null,
            relatedMissionId = 99,
            title = "Mission Deadline",
            message = "Your mission deadline is approaching!",
            scheduledTime = 1702900800000L,
            isDelivered = false,
            isRead = true,
            createdAt = 1702800000000L
        )
        
        val entity = NotificationEntityMapper.fromDomain(notification)
        
        assertEquals(1L, entity.id)
        assertEquals("MISSION_DEADLINE_WARNING", entity.type)
        assertNull(entity.relatedTaskId)
        assertEquals(99, entity.relatedMissionId)
        assertEquals("Mission Deadline", entity.title)
        assertEquals("Your mission deadline is approaching!", entity.message)
        assertEquals(1702900800000L, entity.scheduledTime)
        assertEquals(false, entity.isDelivered)
        assertTrue(entity.isRead)
        assertEquals(1702800000000L, entity.createdAt)
    }

    @Test
    fun `roundtrip mapping preserves all fields`() {
        val original = Notification(
            id = 42L,
            type = NotificationType.MISSION_DAILY_SUMMARY,
            relatedTaskId = null,
            relatedMissionId = null,
            title = "Daily Summary",
            message = "You have 5 missions today",
            scheduledTime = 1702900800000L,
            isDelivered = true,
            isRead = true,
            createdAt = 1702800000000L
        )
        
        val entity = NotificationEntityMapper.fromDomain(original)
        val mapped = NotificationEntityMapper.toDomain(entity)
        
        assertEquals(original.id, mapped.id)
        assertEquals(original.type, mapped.type)
        assertEquals(original.relatedTaskId, mapped.relatedTaskId)
        assertEquals(original.relatedMissionId, mapped.relatedMissionId)
        assertEquals(original.title, mapped.title)
        assertEquals(original.message, mapped.message)
        assertEquals(original.scheduledTime, mapped.scheduledTime)
        assertEquals(original.isDelivered, mapped.isDelivered)
        assertEquals(original.isRead, mapped.isRead)
        assertEquals(original.createdAt, mapped.createdAt)
    }

    @Test
    fun `all notification types are properly converted`() {
        NotificationType.values().forEach { notificationType ->
            val notification = Notification(
                id = 1L,
                type = notificationType,
                relatedTaskId = null,
                relatedMissionId = null,
                title = "Test",
                message = "Test message",
                scheduledTime = 1702900800000L,
                isDelivered = false,
                isRead = false,
                createdAt = 1702800000000L
            )
            
            val entity = NotificationEntityMapper.fromDomain(notification)
            val mapped = NotificationEntityMapper.toDomain(entity)
            
            assertEquals(notificationType, mapped.type)
        }
    }

    @Test
    fun `nullable related IDs are preserved`() {
        val notificationWithTaskId = Notification(
            id = 1L,
            type = NotificationType.TASK_REMINDER,
            relatedTaskId = 10,
            relatedMissionId = null,
            title = "Test",
            message = "Test",
            scheduledTime = 1702900800000L
        )
        
        val notificationWithMissionId = Notification(
            id = 2L,
            type = NotificationType.MISSION_OVERDUE,
            relatedTaskId = null,
            relatedMissionId = 20,
            title = "Test",
            message = "Test",
            scheduledTime = 1702900800000L
        )
        
        val mappedTask = NotificationEntityMapper.toDomain(
            NotificationEntityMapper.fromDomain(notificationWithTaskId)
        )
        val mappedMission = NotificationEntityMapper.toDomain(
            NotificationEntityMapper.fromDomain(notificationWithMissionId)
        )
        
        assertEquals(10, mappedTask.relatedTaskId)
        assertNull(mappedTask.relatedMissionId)
        assertNull(mappedMission.relatedTaskId)
        assertEquals(20, mappedMission.relatedMissionId)
    }
}
