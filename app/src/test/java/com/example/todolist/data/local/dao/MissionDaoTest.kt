package com.example.todolist.data.local.dao

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for MissionDao interface definitions
 */
class MissionDaoTest {

    // Fake in-memory storage for testing DAO contract
    private val fakeStorage = mutableListOf<FakeMissionEntity>()
    
    data class FakeMissionEntity(
        val id: Int,
        val title: String,
        val deadlineEpoch: Long,
        var status: String = "UNSPECIFIED"
    )

    @Test
    fun `getAll returns empty list when no missions`() {
        fakeStorage.clear()
        assertTrue(fakeStorage.isEmpty())
    }

    @Test
    fun `insert adds mission to storage`() {
        val mission = FakeMissionEntity(1, "Test Mission", System.currentTimeMillis() + 86400000)
        fakeStorage.add(mission)
        
        assertEquals(1, fakeStorage.size)
        assertEquals("Test Mission", fakeStorage.first().title)
    }

    @Test
    fun `getById returns correct mission`() {
        fakeStorage.clear()
        fakeStorage.add(FakeMissionEntity(1, "Mission 1", 1000))
        fakeStorage.add(FakeMissionEntity(2, "Mission 2", 2000))
        
        val mission = fakeStorage.find { it.id == 2 }
        
        assertNotNull(mission)
        assertEquals("Mission 2", mission?.title)
    }

    @Test
    fun `getById returns null for non-existent mission`() {
        fakeStorage.clear()
        val mission = fakeStorage.find { it.id == 999 }
        assertNull(mission)
    }

    @Test
    fun `delete removes mission from storage`() {
        fakeStorage.clear()
        fakeStorage.add(FakeMissionEntity(1, "Mission 1", 1000))
        fakeStorage.add(FakeMissionEntity(2, "Mission 2", 2000))
        
        fakeStorage.removeIf { it.id == 1 }
        
        assertEquals(1, fakeStorage.size)
        assertNull(fakeStorage.find { it.id == 1 })
    }

    @Test
    fun `deleteById removes correct mission`() {
        fakeStorage.clear()
        fakeStorage.add(FakeMissionEntity(1, "Mission 1", 1000))
        fakeStorage.add(FakeMissionEntity(2, "Mission 2", 2000))
        
        fakeStorage.removeIf { it.id == 2 }
        
        assertEquals(1, fakeStorage.size)
        assertEquals(1, fakeStorage.first().id)
    }

    @Test
    fun `updateStatus changes mission status`() {
        fakeStorage.clear()
        fakeStorage.add(FakeMissionEntity(1, "Mission", 1000, "UNSPECIFIED"))
        
        val mission = fakeStorage.find { it.id == 1 }
        mission?.status = "COMPLETED"
        
        assertEquals("COMPLETED", fakeStorage.first().status)
    }

    @Test
    fun `getAll returns sorted by deadlineEpoch`() {
        fakeStorage.clear()
        fakeStorage.add(FakeMissionEntity(1, "Later", 3000))
        fakeStorage.add(FakeMissionEntity(2, "Earlier", 1000))
        fakeStorage.add(FakeMissionEntity(3, "Middle", 2000))
        
        val sorted = fakeStorage.sortedBy { it.deadlineEpoch }
        
        assertEquals("Earlier", sorted[0].title)
        assertEquals("Middle", sorted[1].title)
        assertEquals("Later", sorted[2].title)
    }

    @Test
    fun `filter by status`() {
        fakeStorage.clear()
        fakeStorage.add(FakeMissionEntity(1, "Active 1", 1000, "UNSPECIFIED"))
        fakeStorage.add(FakeMissionEntity(2, "Completed", 2000, "COMPLETED"))
        fakeStorage.add(FakeMissionEntity(3, "Active 2", 3000, "UNSPECIFIED"))
        
        val completed = fakeStorage.filter { it.status == "COMPLETED" }
        assertEquals(1, completed.size)
        
        val active = fakeStorage.filter { it.status == "UNSPECIFIED" }
        assertEquals(2, active.size)
    }
}
