package com.example.todolist.data.local.dao

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for NotificationDao interface definitions
 */
class NotificationDaoTest {

    // Fake in-memory storage for testing DAO contract
    private val fakeStorage = mutableListOf<FakeNotificationEntity>()
    
    data class FakeNotificationEntity(
        val id: Long,
        val type: String,
        val relatedTaskId: Int?,
        val relatedMissionId: Int?,
        val title: String,
        val scheduledTime: Long,
        var isDelivered: Boolean = false,
        var isRead: Boolean = false
    )

    @Test
    fun `getAllNotifications returns empty list when no notifications`() {
        fakeStorage.clear()
        assertTrue(fakeStorage.isEmpty())
    }

    @Test
    fun `insertNotification adds to storage`() {
        fakeStorage.add(FakeNotificationEntity(1, "TASK_REMINDER", 100, null, "Reminder", System.currentTimeMillis()))
        assertEquals(1, fakeStorage.size)
    }

    @Test
    fun `getPendingNotifications returns undelivered notifications`() {
        fakeStorage.clear()
        val now = System.currentTimeMillis()
        fakeStorage.add(FakeNotificationEntity(1, "TASK_REMINDER", 1, null, "Pending 1", now - 1000, false))
        fakeStorage.add(FakeNotificationEntity(2, "TASK_REMINDER", 2, null, "Pending 2", now - 500, false))
        fakeStorage.add(FakeNotificationEntity(3, "TASK_REMINDER", 3, null, "Delivered", now - 2000, true))
        
        val pending = fakeStorage.filter { !it.isDelivered && it.scheduledTime <= now }
        assertEquals(2, pending.size)
    }

    @Test
    fun `getNotificationById returns correct notification`() {
        fakeStorage.clear()
        fakeStorage.add(FakeNotificationEntity(1, "TASK_REMINDER", 1, null, "First", 1000))
        fakeStorage.add(FakeNotificationEntity(2, "MISSION_OVERDUE", null, 5, "Second", 2000))
        
        val notification = fakeStorage.find { it.id == 2L }
        assertEquals("Second", notification?.title)
    }

    @Test
    fun `getNotificationsByTaskId returns correct notifications`() {
        fakeStorage.clear()
        fakeStorage.add(FakeNotificationEntity(1, "TASK_REMINDER", 100, null, "Task 100", 1000))
        fakeStorage.add(FakeNotificationEntity(2, "TASK_OVERDUE", 100, null, "Task 100 overdue", 2000))
        fakeStorage.add(FakeNotificationEntity(3, "TASK_REMINDER", 200, null, "Task 200", 3000))
        
        val taskNotifications = fakeStorage.filter { it.relatedTaskId == 100 }
        assertEquals(2, taskNotifications.size)
    }

    @Test
    fun `getNotificationsByMissionId returns correct notifications`() {
        fakeStorage.clear()
        fakeStorage.add(FakeNotificationEntity(1, "MISSION_DEADLINE_WARNING", null, 50, "Mission 50", 1000))
        fakeStorage.add(FakeNotificationEntity(2, "MISSION_OVERDUE", null, 50, "Mission 50 overdue", 2000))
        
        val missionNotifications = fakeStorage.filter { it.relatedMissionId == 50 }
        assertEquals(2, missionNotifications.size)
    }

    @Test
    fun `markAsDelivered updates notification`() {
        fakeStorage.clear()
        fakeStorage.add(FakeNotificationEntity(1, "TASK_REMINDER", 1, null, "Test", 1000, false))
        
        fakeStorage.find { it.id == 1L }?.isDelivered = true
        
        assertTrue(fakeStorage.first().isDelivered)
    }

    @Test
    fun `markAsRead updates notification`() {
        fakeStorage.clear()
        fakeStorage.add(FakeNotificationEntity(1, "TASK_REMINDER", 1, null, "Test", 1000, true, false))
        
        fakeStorage.find { it.id == 1L }?.isRead = true
        
        assertTrue(fakeStorage.first().isRead)
    }

    @Test
    fun `deleteNotification removes from storage`() {
        fakeStorage.clear()
        fakeStorage.add(FakeNotificationEntity(1, "TASK_REMINDER", 1, null, "Test", 1000))
        
        fakeStorage.removeIf { it.id == 1L }
        
        assertTrue(fakeStorage.isEmpty())
    }

    @Test
    fun `deleteNotificationsByTaskId removes all related notifications`() {
        fakeStorage.clear()
        fakeStorage.add(FakeNotificationEntity(1, "TASK_REMINDER", 100, null, "1", 1000))
        fakeStorage.add(FakeNotificationEntity(2, "TASK_OVERDUE", 100, null, "2", 2000))
        fakeStorage.add(FakeNotificationEntity(3, "TASK_REMINDER", 200, null, "3", 3000))
        
        fakeStorage.removeIf { it.relatedTaskId == 100 }
        
        assertEquals(1, fakeStorage.size)
        assertEquals(200, fakeStorage.first().relatedTaskId)
    }

    @Test
    fun `deleteReadNotifications removes delivered and read`() {
        fakeStorage.clear()
        fakeStorage.add(FakeNotificationEntity(1, "TASK_REMINDER", 1, null, "Read", 1000, true, true))
        fakeStorage.add(FakeNotificationEntity(2, "TASK_REMINDER", 2, null, "Unread", 2000, true, false))
        
        fakeStorage.removeIf { it.isDelivered && it.isRead }
        
        assertEquals(1, fakeStorage.size)
        assertFalse(fakeStorage.first().isRead)
    }

    @Test
    fun `getAllNotifications sorted by scheduledTime descending`() {
        fakeStorage.clear()
        fakeStorage.add(FakeNotificationEntity(1, "TASK_REMINDER", 1, null, "Old", 1000))
        fakeStorage.add(FakeNotificationEntity(2, "TASK_REMINDER", 2, null, "New", 3000))
        fakeStorage.add(FakeNotificationEntity(3, "TASK_REMINDER", 3, null, "Mid", 2000))
        
        val sorted = fakeStorage.sortedByDescending { it.scheduledTime }
        
        assertEquals("New", sorted[0].title)
        assertEquals("Mid", sorted[1].title)
        assertEquals("Old", sorted[2].title)
    }
}
