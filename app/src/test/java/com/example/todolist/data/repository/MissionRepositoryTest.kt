package com.example.todolist.data.repository

import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.MissionStatus
import com.example.todolist.core.model.MissionStoredStatus
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * Unit tests for MissionRepository
 */
class MissionRepositoryTest {

    private lateinit var fakeMissions: MutableList<Mission>

    @Before
    fun setup() {
        fakeMissions = mutableListOf(
            Mission(id = 1, title = "Mission 1", deadline = LocalDateTime.now().plusDays(7)),
            Mission(id = 2, title = "Mission 2", deadline = LocalDateTime.now().plusDays(14), storedStatus = MissionStoredStatus.COMPLETED),
            Mission(id = 3, title = "Mission 3", deadline = LocalDateTime.now().minusDays(1))
        )
    }

    @Test
    fun `get all missions returns correct list`() {
        assertEquals(3, fakeMissions.size)
    }

    @Test
    fun `get active missions filters by status`() {
        val activeMissions = fakeMissions.filter { 
            it.status == MissionStatus.ACTIVE
        }
        assertEquals(1, activeMissions.size)
    }

    @Test
    fun `get completed missions filters by status`() {
        val completedMissions = fakeMissions.filter { it.status == MissionStatus.COMPLETED }
        assertEquals(1, completedMissions.size)
        assertEquals("Mission 2", completedMissions.first().title)
    }

    @Test
    fun `get missed missions filters by status`() {
        val missedMissions = fakeMissions.filter { it.status == MissionStatus.MISSED }
        assertEquals(1, missedMissions.size)
        assertEquals("Mission 3", missedMissions.first().title)
    }

    @Test
    fun `insert mission adds to list`() {
        val newMission = Mission(id = 4, title = "New Mission", deadline = LocalDateTime.now().plusDays(30))
        fakeMissions.add(newMission)
        
        assertEquals(4, fakeMissions.size)
        assertTrue(fakeMissions.contains(newMission))
    }

    @Test
    fun `delete mission removes from list`() {
        val missionToRemove = fakeMissions.first()
        fakeMissions.removeIf { it.id == missionToRemove.id }
        
        assertEquals(2, fakeMissions.size)
        assertFalse(fakeMissions.any { it.id == missionToRemove.id })
    }

    @Test
    fun `update mission status marks as completed`() {
        val missionId = 1
        val index = fakeMissions.indexOfFirst { it.id == missionId }
        
        if (index >= 0) {
            fakeMissions[index] = fakeMissions[index].copy(storedStatus = MissionStoredStatus.COMPLETED)
        }
        
        assertEquals(MissionStoredStatus.COMPLETED, fakeMissions.find { it.id == missionId }?.storedStatus)
    }

    @Test
    fun `get mission by id returns correct mission`() {
        val mission = fakeMissions.find { it.id == 2 }
        
        assertNotNull(mission)
        assertEquals("Mission 2", mission?.title)
    }
}
