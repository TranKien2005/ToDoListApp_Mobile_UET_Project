package com.example.todolist.data.mapper

import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.MissionStoredStatus
import com.example.todolist.data.local.entity.MissionEntity
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

/**
 * Unit tests for MissionEntityMapper
 */
class MissionEntityMapperTest {

    @Test
    fun `fromDomain converts Mission to MissionEntity`() {
        val mission = Mission(
            id = 1,
            title = "Test Mission",
            description = "Description",
            deadline = LocalDateTime.of(2024, 12, 25, 23, 59),
            storedStatus = MissionStoredStatus.UNSPECIFIED,
            images = listOf("image1.jpg")
        )

        val entity = MissionEntityMapper.fromDomain(mission)

        assertEquals(1, entity.id)
        assertEquals("Test Mission", entity.title)
        assertEquals("Description", entity.description)
        assertEquals("UNSPECIFIED", entity.status)
        assertNotNull(entity.images)
    }

    @Test
    fun `toDomain converts MissionEntity to Mission`() {
        val entity = MissionEntity(
            id = 2,
            title = "Entity Mission",
            description = "Desc",
            deadlineEpoch = System.currentTimeMillis() + 86400000,
            status = "COMPLETED",
            images = "[\"img.jpg\"]"
        )

        val mission = MissionEntityMapper.toDomain(entity)

        assertEquals(2, mission.id)
        assertEquals("Entity Mission", mission.title)
        assertEquals("Desc", mission.description)
        assertEquals(MissionStoredStatus.COMPLETED, mission.storedStatus)
        assertEquals(1, mission.images.size)
    }

    @Test
    fun `toDomain handles null description`() {
        val entity = MissionEntity(
            id = 1,
            title = "Mission",
            description = null,
            deadlineEpoch = System.currentTimeMillis() + 86400000
        )

        val mission = MissionEntityMapper.toDomain(entity)

        assertNull(mission.description)
    }

    @Test
    fun `toDomain handles empty images`() {
        val entity = MissionEntity(
            id = 1,
            title = "Mission",
            description = null,
            deadlineEpoch = System.currentTimeMillis() + 86400000,
            images = null
        )

        val mission = MissionEntityMapper.toDomain(entity)

        assertTrue(mission.images.isEmpty())
    }

    @Test
    fun `toDomain handles invalid status`() {
        val entity = MissionEntity(
            id = 1,
            title = "Mission",
            description = null,
            deadlineEpoch = System.currentTimeMillis() + 86400000,
            status = "INVALID"
        )

        val mission = MissionEntityMapper.toDomain(entity)

        assertEquals(MissionStoredStatus.UNSPECIFIED, mission.storedStatus)
    }

    @Test
    fun `round trip conversion preserves data`() {
        val originalMission = Mission(
            id = 5,
            title = "Round Trip",
            description = "Test",
            deadline = LocalDateTime.now().plusDays(7),
            storedStatus = MissionStoredStatus.COMPLETED,
            images = listOf("a.jpg", "b.jpg")
        )

        val entity = MissionEntityMapper.fromDomain(originalMission)
        val convertedMission = MissionEntityMapper.toDomain(entity)

        assertEquals(originalMission.id, convertedMission.id)
        assertEquals(originalMission.title, convertedMission.title)
        assertEquals(originalMission.description, convertedMission.description)
        assertEquals(originalMission.storedStatus, convertedMission.storedStatus)
        assertEquals(originalMission.images.size, convertedMission.images.size)
    }
}
