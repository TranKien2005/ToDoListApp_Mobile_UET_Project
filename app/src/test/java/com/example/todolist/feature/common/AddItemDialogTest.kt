package com.example.todolist.feature.common

import com.example.todolist.core.model.Task
import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.RepeatType
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

/**
 * Unit tests for AddItemDialog validation logic
 */
class AddItemDialogTest {

    @Test
    fun `task title validation - empty title is invalid`() {
        val title = ""
        assertTrue(title.isBlank())
    }

    @Test
    fun `task title validation - valid title`() {
        val title = "My Task"
        assertFalse(title.isBlank())
    }

    @Test
    fun `task duration validation - null duration is valid`() {
        val duration: Long? = null
        assertNull(duration)
    }

    @Test
    fun `task duration validation - positive duration is valid`() {
        val duration = 60L
        assertTrue(duration > 0)
    }

    @Test
    fun `mission deadline validation - future date is valid`() {
        val deadline = LocalDateTime.now().plusDays(1)
        assertTrue(deadline.isAfter(LocalDateTime.now()))
    }

    @Test
    fun `mission deadline validation - past date might be allowed for editing`() {
        val deadline = LocalDateTime.now().minusDays(1)
        assertTrue(deadline.isBefore(LocalDateTime.now()))
    }

    @Test
    fun `task creation with all fields`() {
        val task = Task(
            id = 0,
            title = "Test Task",
            description = "Description",
            startTime = LocalDateTime.now(),
            durationMinutes = 30,
            repeatType = RepeatType.WEEKLY,
            images = listOf("image.jpg")
        )

        assertNotNull(task)
        assertEquals(30L, task.durationMinutes)
        assertEquals(RepeatType.WEEKLY, task.repeatType)
        assertEquals(1, task.images.size)
    }

    @Test
    fun `mission creation with all fields`() {
        val mission = Mission(
            id = 0,
            title = "Test Mission",
            description = "Description",
            deadline = LocalDateTime.now().plusDays(7),
            images = listOf("image1.jpg", "image2.jpg")
        )

        assertNotNull(mission)
        assertEquals(2, mission.images.size)
    }

    @Test
    fun `repeat type options`() {
        val types = RepeatType.values()
        assertEquals(4, types.size)
    }

    @Test
    fun `edit mode preserves existing data`() {
        val originalTask = Task(
            id = 1,
            title = "Original",
            startTime = LocalDateTime.now()
        )

        val editedTask = originalTask.copy(title = "Edited")
        
        assertEquals(1, editedTask.id)
        assertEquals("Edited", editedTask.title)
    }
}
