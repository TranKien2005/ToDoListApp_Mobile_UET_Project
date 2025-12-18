package com.example.todolist.integration

import com.example.todolist.core.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

/**
 * Integration tests for app data flow
 */
class AppIntegrationTest {

    @Test
    fun `task creation flow`() {
        val task = Task(
            id = 0,
            title = "New Task",
            description = "Description",
            startTime = LocalDateTime.now(),
            durationMinutes = 60,
            repeatType = RepeatType.DAILY
        )

        assertNotNull(task)
        assertEquals("New Task", task.title)
        assertEquals(60L, task.durationMinutes)
        assertEquals(RepeatType.DAILY, task.repeatType)
    }

    @Test
    fun `mission creation flow`() {
        val mission = Mission(
            id = 0,
            title = "New Mission",
            description = "Description",
            deadline = LocalDateTime.now().plusDays(7),
            storedStatus = MissionStoredStatus.UNSPECIFIED
        )

        assertNotNull(mission)
        assertEquals("New Mission", mission.title)
        assertEquals(MissionStoredStatus.UNSPECIFIED, mission.storedStatus)
    }

    @Test
    fun `user onboarding flow`() {
        val user = User(
            id = 0,
            fullName = "Test User",
            age = 25,
            gender = Gender.MALE
        )

        assertNotNull(user)
        assertEquals("Test User", user.fullName)
    }

    @Test
    fun `settings update flow`() {
        var settings = Settings()
        
        // Update language
        settings = settings.copy(language = AppLanguage.VIETNAMESE)
        assertEquals(AppLanguage.VIETNAMESE, settings.language)
        
        // Update reminder
        settings = settings.copy(taskReminderMinutes = 30)
        assertEquals(30, settings.taskReminderMinutes)
    }

    @Test
    fun `mission status transition flow`() {
        // Create active mission
        val activeMission = Mission(
            id = 1,
            title = "Active Mission",
            deadline = LocalDateTime.now().plusDays(1),
            storedStatus = MissionStoredStatus.UNSPECIFIED
        )
        assertEquals(MissionStatus.ACTIVE, activeMission.status)

        // Complete mission
        val completedMission = activeMission.copy(storedStatus = MissionStoredStatus.COMPLETED)
        assertEquals(MissionStatus.COMPLETED, completedMission.status)
    }

    @Test
    fun `notification creation flow`() {
        val notification = Notification(
            type = NotificationType.TASK_REMINDER,
            title = "Task Reminder",
            message = "You have a task in 15 minutes",
            scheduledTime = System.currentTimeMillis() + 900000,
            isDelivered = false,
            isRead = false
        )

        assertNotNull(notification)
        assertFalse(notification.isDelivered)
        assertFalse(notification.isRead)
    }

    @Test
    fun `task with images flow`() {
        val task = Task(
            id = 1,
            title = "Task with Images",
            startTime = LocalDateTime.now(),
            images = listOf("image1.jpg", "image2.jpg", "image3.jpg")
        )

        assertEquals(3, task.images.size)
        assertTrue(task.images.contains("image1.jpg"))
    }

    @Test
    fun `mission with images flow`() {
        val mission = Mission(
            id = 1,
            title = "Mission with Images",
            deadline = LocalDateTime.now().plusDays(7),
            images = listOf("mission_image.jpg")
        )

        assertEquals(1, mission.images.size)
    }
}
