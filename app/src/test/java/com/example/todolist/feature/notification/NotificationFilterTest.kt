package com.example.todolist.feature.notification

import com.example.todolist.core.model.Notification
import com.example.todolist.core.model.NotificationType
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for notification filtering and sorting logic
 */
class NotificationFilterTest {

    private val now = System.currentTimeMillis()
    
    private val sampleNotifications = listOf(
        Notification(id = 1, type = NotificationType.TASK_REMINDER, title = "Task 1", message = "", scheduledTime = now - 3600000, isRead = false, isDelivered = true),
        Notification(id = 2, type = NotificationType.MISSION_DEADLINE_WARNING, title = "Mission 1", message = "", scheduledTime = now - 1800000, isRead = true, isDelivered = true),
        Notification(id = 3, type = NotificationType.TASK_OVERDUE, title = "Overdue", message = "", scheduledTime = now, isRead = false, isDelivered = true),
        Notification(id = 4, type = NotificationType.MISSION_DAILY_SUMMARY, title = "Summary", message = "", scheduledTime = now - 7200000, isRead = true, isDelivered = true)
    )

    @Test
    fun `filter unread notifications`() {
        val unread = sampleNotifications.filter { !it.isRead }
        assertEquals(2, unread.size)
        assertTrue(unread.all { !it.isRead })
    }

    @Test
    fun `filter read notifications`() {
        val read = sampleNotifications.filter { it.isRead }
        assertEquals(2, read.size)
        assertTrue(read.all { it.isRead })
    }

    @Test
    fun `count unread notifications`() {
        val count = sampleNotifications.count { !it.isRead }
        assertEquals(2, count)
    }

    @Test
    fun `filter by task type`() {
        val taskNotifications = sampleNotifications.filter { 
            it.type == NotificationType.TASK_REMINDER || it.type == NotificationType.TASK_OVERDUE 
        }
        assertEquals(2, taskNotifications.size)
    }

    @Test
    fun `filter by mission type`() {
        val missionNotifications = sampleNotifications.filter { 
            it.type.name.startsWith("MISSION_")
        }
        assertEquals(2, missionNotifications.size)
    }

    @Test
    fun `sort by time descending`() {
        val sorted = sampleNotifications.sortedByDescending { it.scheduledTime }
        
        assertTrue(sorted[0].scheduledTime >= sorted[1].scheduledTime)
        assertTrue(sorted[1].scheduledTime >= sorted[2].scheduledTime)
        assertTrue(sorted[2].scheduledTime >= sorted[3].scheduledTime)
    }

    @Test
    fun `sort by time ascending`() {
        val sorted = sampleNotifications.sortedBy { it.scheduledTime }
        
        assertTrue(sorted[0].scheduledTime <= sorted[1].scheduledTime)
        assertTrue(sorted[1].scheduledTime <= sorted[2].scheduledTime)
    }

    @Test
    fun `filter delivered notifications`() {
        val delivered = sampleNotifications.filter { it.isDelivered }
        assertEquals(4, delivered.size)
    }

    @Test
    fun `mark all as read`() {
        val allRead = sampleNotifications.map { it.copy(isRead = true) }
        assertTrue(allRead.all { it.isRead })
    }

    @Test
    fun `group by type`() {
        val grouped = sampleNotifications.groupBy { it.type }
        
        assertEquals(1, grouped[NotificationType.TASK_REMINDER]?.size)
        assertEquals(1, grouped[NotificationType.MISSION_DEADLINE_WARNING]?.size)
        assertEquals(1, grouped[NotificationType.TASK_OVERDUE]?.size)
        assertEquals(1, grouped[NotificationType.MISSION_DAILY_SUMMARY]?.size)
    }
}
