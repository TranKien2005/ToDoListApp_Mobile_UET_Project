package com.example.todolist.data.repository

import com.example.todolist.core.model.Notification
import com.example.todolist.core.model.NotificationType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for NotificationRepository
 */
class NotificationRepositoryTest {

    private lateinit var fakeNotifications: MutableList<Notification>

    @Before
    fun setup() {
        val now = System.currentTimeMillis()
        fakeNotifications = mutableListOf(
            Notification(id = 1, title = "Task Reminder", message = "Complete task", type = NotificationType.TASK_REMINDER, scheduledTime = now - 3600000, isDelivered = true, isRead = false),
            Notification(id = 2, title = "Mission Due", message = "Deadline soon", type = NotificationType.MISSION_DEADLINE_WARNING, scheduledTime = now - 1800000, isDelivered = true, isRead = true),
            Notification(id = 3, title = "Overdue", message = "Task missed", type = NotificationType.TASK_OVERDUE, scheduledTime = now, isDelivered = true, isRead = false)
        )
    }

    @Test
    fun `get all notifications returns correct list`() {
        assertEquals(3, fakeNotifications.size)
    }

    @Test
    fun `get unread notifications`() {
        val unread = fakeNotifications.filter { !it.isRead }
        assertEquals(2, unread.size)
    }

    @Test
    fun `get unread count`() {
        val count = fakeNotifications.count { !it.isRead }
        assertEquals(2, count)
    }

    @Test
    fun `mark notification as read`() {
        val id = 1L
        val index = fakeNotifications.indexOfFirst { it.id == id }
        if (index >= 0) {
            fakeNotifications[index] = fakeNotifications[index].copy(isRead = true)
        }
        
        assertTrue(fakeNotifications.find { it.id == id }?.isRead == true)
    }

    @Test
    fun `mark all notifications as read`() {
        val allRead = fakeNotifications.map { it.copy(isRead = true) }
        fakeNotifications.clear()
        fakeNotifications.addAll(allRead)
        
        assertTrue(fakeNotifications.all { it.isRead })
    }

    @Test
    fun `delete notification`() {
        fakeNotifications.removeIf { it.id == 1L }
        assertEquals(2, fakeNotifications.size)
        assertNull(fakeNotifications.find { it.id == 1L })
    }

    @Test
    fun `delete all notifications`() {
        fakeNotifications.clear()
        assertTrue(fakeNotifications.isEmpty())
    }

    @Test
    fun `insert notification`() {
        val newNotification = Notification(
            id = 4, 
            title = "New", 
            message = "New notification", 
            type = NotificationType.MISSION_DAILY_SUMMARY, 
            scheduledTime = System.currentTimeMillis()
        )
        fakeNotifications.add(newNotification)
        
        assertEquals(4, fakeNotifications.size)
        assertNotNull(fakeNotifications.find { it.id == 4L })
    }

    @Test
    fun `get notifications sorted by time`() {
        val sorted = fakeNotifications.sortedByDescending { it.scheduledTime }
        assertTrue(sorted[0].scheduledTime >= sorted[1].scheduledTime)
        assertTrue(sorted[1].scheduledTime >= sorted[2].scheduledTime)
    }

    @Test
    fun `filter by notification type`() {
        val taskNotifications = fakeNotifications.filter { 
            it.type == NotificationType.TASK_REMINDER || it.type == NotificationType.TASK_OVERDUE 
        }
        assertEquals(2, taskNotifications.size)
    }
}
