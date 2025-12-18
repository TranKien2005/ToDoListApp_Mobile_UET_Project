package com.example.todolist.core.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for MissionStatus and MissionStoredStatus enums
 */
class MissionStatusTest {

    @Test
    fun `mission status has all values`() {
        val statuses = MissionStatus.values()
        assertEquals(3, statuses.size)
        assertTrue(statuses.contains(MissionStatus.ACTIVE))
        assertTrue(statuses.contains(MissionStatus.COMPLETED))
        assertTrue(statuses.contains(MissionStatus.MISSED))
    }

    @Test
    fun `mission stored status has all values`() {
        val statuses = MissionStoredStatus.values()
        assertEquals(2, statuses.size)
        assertTrue(statuses.contains(MissionStoredStatus.UNSPECIFIED))
        assertTrue(statuses.contains(MissionStoredStatus.COMPLETED))
    }

    @Test
    fun `mission status ordinals`() {
        assertEquals(0, MissionStatus.ACTIVE.ordinal)
        assertEquals(1, MissionStatus.COMPLETED.ordinal)
        assertEquals(2, MissionStatus.MISSED.ordinal)
    }

    @Test
    fun `mission stored status ordinals`() {
        assertEquals(0, MissionStoredStatus.UNSPECIFIED.ordinal)
        assertEquals(1, MissionStoredStatus.COMPLETED.ordinal)
    }

    @Test
    fun `valueOf returns correct enum`() {
        assertEquals(MissionStatus.ACTIVE, MissionStatus.valueOf("ACTIVE"))
        assertEquals(MissionStatus.COMPLETED, MissionStatus.valueOf("COMPLETED"))
        assertEquals(MissionStatus.MISSED, MissionStatus.valueOf("MISSED"))
    }
}
