package com.example.todolist.data.mapper

import com.example.todolist.core.model.Task
import com.example.todolist.core.model.RepeatType
import com.example.todolist.data.local.entity.TaskEntity
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

/**
 * Unit tests for TaskEntityMapper
 */
class TaskEntityMapperTest {

    @Test
    fun `fromDomain converts Task to TaskEntity`() {
        val task = Task(
            id = 1,
            title = "Test Task",
            description = "Description",
            startTime = LocalDateTime.of(2024, 12, 18, 10, 0),
            durationMinutes = 60L,
            repeatType = RepeatType.DAILY,
            images = listOf("image1.jpg", "image2.jpg")
        )

        val entity = TaskEntityMapper.fromDomain(task)

        assertEquals(1, entity.id)
        assertEquals("Test Task", entity.title)
        assertEquals("Description", entity.description)
        assertEquals(60L, entity.durationMinutes)
        assertEquals("DAILY", entity.repeatType)
        assertNotNull(entity.images)
    }

    @Test
    fun `toDomain converts TaskEntity to Task`() {
        val entity = TaskEntity(
            id = 2,
            title = "Entity Task",
            description = "Desc",
            startTimeEpoch = System.currentTimeMillis(),
            durationMinutes = 30L,
            repeatType = "WEEKLY",
            images = "[\"img.jpg\"]"
        )

        val task = TaskEntityMapper.toDomain(entity)

        assertEquals(2, task.id)
        assertEquals("Entity Task", task.title)
        assertEquals("Desc", task.description)
        assertEquals(30L, task.durationMinutes)
        assertEquals(RepeatType.WEEKLY, task.repeatType)
        assertEquals(1, task.images.size)
    }

    @Test
    fun `toDomain handles null duration`() {
        val entity = TaskEntity(
            id = 1,
            title = "Task",
            description = null,
            startTimeEpoch = System.currentTimeMillis(),
            durationMinutes = null
        )

        val task = TaskEntityMapper.toDomain(entity)

        assertNull(task.durationMinutes)
        assertNull(task.description)
    }

    @Test
    fun `toDomain handles empty images`() {
        val entity = TaskEntity(
            id = 1,
            title = "Task",
            description = null,
            startTimeEpoch = System.currentTimeMillis(),
            durationMinutes = null,
            images = null
        )

        val task = TaskEntityMapper.toDomain(entity)

        assertTrue(task.images.isEmpty())
    }

    @Test
    fun `toDomain handles invalid repeat type`() {
        val entity = TaskEntity(
            id = 1,
            title = "Task",
            description = null,
            startTimeEpoch = System.currentTimeMillis(),
            durationMinutes = null,
            repeatType = "INVALID"
        )

        val task = TaskEntityMapper.toDomain(entity)

        assertEquals(RepeatType.NONE, task.repeatType)
    }

    @Test
    fun `round trip conversion preserves data`() {
        val originalTask = Task(
            id = 5,
            title = "Round Trip",
            description = "Test",
            startTime = LocalDateTime.now(),
            durationMinutes = 45L,
            repeatType = RepeatType.MONTHLY,
            images = listOf("a.jpg", "b.jpg")
        )

        val entity = TaskEntityMapper.fromDomain(originalTask)
        val convertedTask = TaskEntityMapper.toDomain(entity)

        assertEquals(originalTask.id, convertedTask.id)
        assertEquals(originalTask.title, convertedTask.title)
        assertEquals(originalTask.description, convertedTask.description)
        assertEquals(originalTask.durationMinutes, convertedTask.durationMinutes)
        assertEquals(originalTask.repeatType, convertedTask.repeatType)
        assertEquals(originalTask.images.size, convertedTask.images.size)
    }
}
