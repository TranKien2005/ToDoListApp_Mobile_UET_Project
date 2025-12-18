package com.example.todolist.core.model

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

/**
 * Unit tests for Mission model
 */
class MissionTest {

    @Test
    fun `create mission with all fields`() {
        val deadline = LocalDateTime.now().plusDays(7)
        val mission = Mission(
            id = 1,
            title = "Test Mission",
            description = "Test Description",
            deadline = deadline,
            storedStatus = MissionStoredStatus.UNSPECIFIED,
            images = listOf("image1.jpg")
        )

        assertEquals(1, mission.id)
        assertEquals("Test Mission", mission.title)
        assertEquals("Test Description", mission.description)
        assertEquals(deadline, mission.deadline)
        assertEquals(MissionStoredStatus.UNSPECIFIED, mission.storedStatus)
        assertEquals(1, mission.images.size)
    }

    @Test
    fun `mission status computed correctly for ACTIVE`() {
        val futureDeadline = LocalDateTime.now().plusDays(1)
        val mission = Mission(
            id = 1,
            title = "Active Mission",
            deadline = futureDeadline,
            storedStatus = MissionStoredStatus.UNSPECIFIED
        )

        assertEquals(MissionStatus.ACTIVE, mission.status)
    }

    @Test
    fun `mission status computed correctly for COMPLETED`() {
        val deadline = LocalDateTime.now().plusDays(1)
        val mission = Mission(
            id = 1,
            title = "Completed Mission",
            deadline = deadline,
            storedStatus = MissionStoredStatus.COMPLETED
        )

        assertEquals(MissionStatus.COMPLETED, mission.status)
    }

    @Test
    fun `mission status MISSED when deadline passed and not completed`() {
        val pastDeadline = LocalDateTime.now().minusDays(1)
        val mission = Mission(
            id = 1,
            title = "Missed Mission",
            deadline = pastDeadline,
            storedStatus = MissionStoredStatus.UNSPECIFIED
        )

        assertEquals(MissionStatus.MISSED, mission.status)
    }

    @Test
    fun `mission with default values`() {
        val deadline = LocalDateTime.now().plusDays(1)
        val mission = Mission(
            id = 0,
            title = "Simple Mission",
            deadline = deadline
        )

        assertNull(mission.description)
        assertEquals(MissionStoredStatus.UNSPECIFIED, mission.storedStatus)
        assertTrue(mission.images.isEmpty())
    }

    @Test
    fun `mission stored status enum values`() {
        assertEquals(2, MissionStoredStatus.values().size)
        assertTrue(MissionStoredStatus.values().contains(MissionStoredStatus.UNSPECIFIED))
        assertTrue(MissionStoredStatus.values().contains(MissionStoredStatus.COMPLETED))
    }

    @Test
    fun `mission status enum values`() {
        assertEquals(3, MissionStatus.values().size)
        assertTrue(MissionStatus.values().contains(MissionStatus.ACTIVE))
        assertTrue(MissionStatus.values().contains(MissionStatus.COMPLETED))
        assertTrue(MissionStatus.values().contains(MissionStatus.MISSED))
    }
}
