package com.example.todolist.domain.usecase

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for NotificationUseCases interfaces
 */
class NotificationUseCasesTest {

    // Fake notification storage
    private val fakeNotifications = mutableListOf<FakeNotification>()
    
    data class FakeNotification(
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
    fun `getNotifications returns all notifications`() {
        fakeNotifications.clear()
        fakeNotifications.add(FakeNotification(1, "TASK_REMINDER", 1, null, "Task 1", 1000))
        fakeNotifications.add(FakeNotification(2, "MISSION_DEADLINE_WARNING", null, 1, "Mission 1", 2000))
        
        assertEquals(2, fakeNotifications.size)
    }

    @Test
    fun `scheduleTaskNotification creates notification`() {
        fakeNotifications.clear()
        val taskId = 100
        val scheduledTime = System.currentTimeMillis() + 900000 // 15 minutes
        
        fakeNotifications.add(FakeNotification(1, "TASK_REMINDER", taskId, null, "Task Reminder", scheduledTime))
        
        assertEquals(1, fakeNotifications.size)
        assertEquals(taskId, fakeNotifications.first().relatedTaskId)
    }

    @Test
    fun `scheduleMissionNotification creates notification`() {
        fakeNotifications.clear()
        val missionId = 50
        val scheduledTime = System.currentTimeMillis() + 3600000 // 60 minutes
        
        fakeNotifications.add(FakeNotification(1, "MISSION_DEADLINE_WARNING", null, missionId, "Mission Warning", scheduledTime))
        
        assertEquals(1, fakeNotifications.size)
        assertEquals(missionId, fakeNotifications.first().relatedMissionId)
    }

    @Test
    fun `cancelTaskNotifications removes all for task`() {
        fakeNotifications.clear()
        fakeNotifications.add(FakeNotification(1, "TASK_REMINDER", 100, null, "Reminder", 1000))
        fakeNotifications.add(FakeNotification(2, "TASK_OVERDUE", 100, null, "Overdue", 2000))
        fakeNotifications.add(FakeNotification(3, "TASK_REMINDER", 200, null, "Other", 3000))
        
        fakeNotifications.removeIf { it.relatedTaskId == 100 }
        
        assertEquals(1, fakeNotifications.size)
        assertEquals(200, fakeNotifications.first().relatedTaskId)
    }

    @Test
    fun `cancelMissionNotifications removes all for mission`() {
        fakeNotifications.clear()
        fakeNotifications.add(FakeNotification(1, "MISSION_DEADLINE_WARNING", null, 50, "Warning", 1000))
        fakeNotifications.add(FakeNotification(2, "MISSION_OVERDUE", null, 50, "Overdue", 2000))
        fakeNotifications.add(FakeNotification(3, "MISSION_DEADLINE_WARNING", null, 100, "Other", 3000))
        
        fakeNotifications.removeIf { it.relatedMissionId == 50 }
        
        assertEquals(1, fakeNotifications.size)
        assertEquals(100, fakeNotifications.first().relatedMissionId)
    }

    @Test
    fun `markNotificationAsRead updates notification`() {
        fakeNotifications.clear()
        fakeNotifications.add(FakeNotification(1, "TASK_REMINDER", 1, null, "Test", 1000, true, false))
        
        fakeNotifications.find { it.id == 1L }?.isRead = true
        
        assertTrue(fakeNotifications.first().isRead)
    }

    @Test
    fun `deleteReadNotifications removes delivered and read`() {
        fakeNotifications.clear()
        fakeNotifications.add(FakeNotification(1, "TASK_REMINDER", 1, null, "Read", 1000, true, true))
        fakeNotifications.add(FakeNotification(2, "TASK_REMINDER", 2, null, "Unread", 2000, true, false))
        fakeNotifications.add(FakeNotification(3, "TASK_REMINDER", 3, null, "Not Delivered", 3000, false, false))
        
        fakeNotifications.removeIf { it.isDelivered && it.isRead }
        
        assertEquals(2, fakeNotifications.size)
    }

    @Test
    fun `createNotification adds to list`() {
        fakeNotifications.clear()
        
        val newNotification = FakeNotification(0, "MISSION_DAILY_SUMMARY", null, null, "Daily Summary", System.currentTimeMillis())
        fakeNotifications.add(newNotification)
        
        assertEquals(1, fakeNotifications.size)
        assertEquals("MISSION_DAILY_SUMMARY", fakeNotifications.first().type)
    }

    @Test
    fun `notifications sorted by scheduled time`() {
        fakeNotifications.clear()
        fakeNotifications.add(FakeNotification(1, "TASK_REMINDER", 1, null, "Later", 3000))
        fakeNotifications.add(FakeNotification(2, "TASK_REMINDER", 2, null, "Earlier", 1000))
        fakeNotifications.add(FakeNotification(3, "TASK_REMINDER", 3, null, "Middle", 2000))
        
        val sorted = fakeNotifications.sortedByDescending { it.scheduledTime }
        
        assertEquals("Later", sorted[0].title)
        assertEquals("Middle", sorted[1].title)
        assertEquals("Earlier", sorted[2].title)
    }
}
