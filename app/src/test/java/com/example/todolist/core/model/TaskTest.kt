package com.example.todolist.core.model

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

/**
 * Unit tests for Task model
 */
class TaskTest {

    @Test
    fun `create task with all fields`() {
        val now = LocalDateTime.now()
        val task = Task(
            id = 1,
            title = "Test Task",
            description = "Test Description",
            startTime = now,
            durationMinutes = 60L,
            repeatType = RepeatType.DAILY,
            images = listOf("image1.jpg", "image2.jpg")
        )

        assertEquals(1, task.id)
        assertEquals("Test Task", task.title)
        assertEquals("Test Description", task.description)
        assertEquals(now, task.startTime)
        assertEquals(60L, task.durationMinutes)
        assertEquals(RepeatType.DAILY, task.repeatType)
        assertEquals(2, task.images.size)
    }

    @Test
    fun `create task with default values`() {
        val now = LocalDateTime.now()
        val task = Task(
            id = 0,
            title = "Simple Task",
            startTime = now
        )

        assertEquals(0, task.id)
        assertNull(task.description)
        assertNull(task.durationMinutes)
        assertEquals(RepeatType.NONE, task.repeatType)
        assertTrue(task.images.isEmpty())
    }

    @Test
    fun `task equality test`() {
        val now = LocalDateTime.now()
        val task1 = Task(id = 1, title = "Task", startTime = now)
        val task2 = Task(id = 1, title = "Task", startTime = now)
        
        assertEquals(task1, task2)
    }

    @Test
    fun `repeat type enum values`() {
        assertEquals(4, RepeatType.values().size)
        assertTrue(RepeatType.values().contains(RepeatType.NONE))
        assertTrue(RepeatType.values().contains(RepeatType.DAILY))
        assertTrue(RepeatType.values().contains(RepeatType.WEEKLY))
        assertTrue(RepeatType.values().contains(RepeatType.MONTHLY))
    }
}
