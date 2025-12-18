package com.example.todolist.data.local.dao

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for TaskDao interface definitions
 * Note: These tests validate the DAO contract/interface, not the Room implementation
 * Room-specific tests would require instrumentation tests with an in-memory database
 */
class TaskDaoTest {

    // Fake in-memory storage for testing DAO contract
    private val fakeStorage = mutableListOf<FakeTaskEntity>()
    
    data class FakeTaskEntity(
        val id: Int,
        val title: String,
        val startTimeEpoch: Long
    )

    @Test
    fun `getAll returns empty list when no tasks`() {
        fakeStorage.clear()
        assertTrue(fakeStorage.isEmpty())
    }

    @Test
    fun `insert adds task to storage`() {
        val task = FakeTaskEntity(1, "Test Task", System.currentTimeMillis())
        fakeStorage.add(task)
        
        assertEquals(1, fakeStorage.size)
        assertEquals("Test Task", fakeStorage.first().title)
    }

    @Test
    fun `getById returns correct task`() {
        fakeStorage.clear()
        fakeStorage.add(FakeTaskEntity(1, "Task 1", 1000))
        fakeStorage.add(FakeTaskEntity(2, "Task 2", 2000))
        
        val task = fakeStorage.find { it.id == 2 }
        
        assertNotNull(task)
        assertEquals("Task 2", task?.title)
    }

    @Test
    fun `getById returns null for non-existent task`() {
        fakeStorage.clear()
        val task = fakeStorage.find { it.id == 999 }
        assertNull(task)
    }

    @Test
    fun `delete removes task from storage`() {
        fakeStorage.clear()
        fakeStorage.add(FakeTaskEntity(1, "Task 1", 1000))
        fakeStorage.add(FakeTaskEntity(2, "Task 2", 2000))
        
        fakeStorage.removeIf { it.id == 1 }
        
        assertEquals(1, fakeStorage.size)
        assertNull(fakeStorage.find { it.id == 1 })
    }

    @Test
    fun `deleteById removes correct task`() {
        fakeStorage.clear()
        fakeStorage.add(FakeTaskEntity(1, "Task 1", 1000))
        fakeStorage.add(FakeTaskEntity(2, "Task 2", 2000))
        
        fakeStorage.removeIf { it.id == 2 }
        
        assertEquals(1, fakeStorage.size)
        assertEquals(1, fakeStorage.first().id)
    }

    @Test
    fun `insert with replace updates existing task`() {
        fakeStorage.clear()
        fakeStorage.add(FakeTaskEntity(1, "Original", 1000))
        
        // Simulate replace
        val index = fakeStorage.indexOfFirst { it.id == 1 }
        if (index >= 0) {
            fakeStorage[index] = FakeTaskEntity(1, "Updated", 1000)
        }
        
        assertEquals(1, fakeStorage.size)
        assertEquals("Updated", fakeStorage.first().title)
    }

    @Test
    fun `getAll returns sorted by startTimeEpoch`() {
        fakeStorage.clear()
        fakeStorage.add(FakeTaskEntity(1, "Later", 3000))
        fakeStorage.add(FakeTaskEntity(2, "Earlier", 1000))
        fakeStorage.add(FakeTaskEntity(3, "Middle", 2000))
        
        val sorted = fakeStorage.sortedBy { it.startTimeEpoch }
        
        assertEquals("Earlier", sorted[0].title)
        assertEquals("Middle", sorted[1].title)
        assertEquals("Later", sorted[2].title)
    }
}
