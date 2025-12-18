package com.example.todolist.data.local.entity

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for MissionEntity
 */
class MissionEntityTest {

    @Test
    fun `create mission entity with all fields`() {
        val entity = MissionEntity(
            id = 1,
            title = "Test Mission",
            description = "Description",
            deadlineEpoch = System.currentTimeMillis() + 86400000,
            status = "COMPLETED",
            images = "[\"image1.jpg\"]"
        )

        assertEquals(1, entity.id)
        assertEquals("Test Mission", entity.title)
        assertEquals("Description", entity.description)
        assertEquals("COMPLETED", entity.status)
        assertNotNull(entity.images)
    }

    @Test
    fun `create mission entity with default values`() {
        val entity = MissionEntity(
            id = 0,
            title = "Simple Mission",
            description = null,
            deadlineEpoch = System.currentTimeMillis() + 86400000
        )

        assertEquals("UNSPECIFIED", entity.status)
        assertNull(entity.images)
    }

    @Test
    fun `mission entity equality`() {
        val deadline = System.currentTimeMillis()
        val entity1 = MissionEntity(id = 1, title = "Test", description = null, deadlineEpoch = deadline)
        val entity2 = MissionEntity(id = 1, title = "Test", description = null, deadlineEpoch = deadline)
        
        assertEquals(entity1, entity2)
    }
}
