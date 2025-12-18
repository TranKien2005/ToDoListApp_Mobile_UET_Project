package com.example.todolist.data.local.entity

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for TaskEntity
 */
class TaskEntityTest {

    @Test
    fun `create task entity with all fields`() {
        val entity = TaskEntity(
            id = 1,
            title = "Test Task",
            description = "Description",
            startTimeEpoch = System.currentTimeMillis(),
            durationMinutes = 60L,
            repeatType = "DAILY",
            images = "[\"image1.jpg\",\"image2.jpg\"]"
        )

        assertEquals(1, entity.id)
        assertEquals("Test Task", entity.title)
        assertEquals("Description", entity.description)
        assertEquals(60L, entity.durationMinutes)
        assertEquals("DAILY", entity.repeatType)
        assertNotNull(entity.images)
    }

    @Test
    fun `create task entity with null optional fields`() {
        val entity = TaskEntity(
            id = 0,
            title = "Simple Task",
            description = null,
            startTimeEpoch = System.currentTimeMillis(),
            durationMinutes = null
        )

        assertNull(entity.description)
        assertNull(entity.durationMinutes)
        assertEquals("NONE", entity.repeatType)
        assertNull(entity.images)
    }

    @Test
    fun `task entity equality`() {
        val time = System.currentTimeMillis()
        val entity1 = TaskEntity(id = 1, title = "Test", description = null, startTimeEpoch = time, durationMinutes = null)
        val entity2 = TaskEntity(id = 1, title = "Test", description = null, startTimeEpoch = time, durationMinutes = null)
        
        assertEquals(entity1, entity2)
    }
}
