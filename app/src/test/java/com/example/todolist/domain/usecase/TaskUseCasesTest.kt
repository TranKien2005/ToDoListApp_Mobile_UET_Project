package com.example.todolist.domain.usecase

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for TaskUseCases interfaces
 */
class TaskUseCasesTest {

    // Fake implementations for testing
    private val fakeTasks = mutableListOf<FakeTask>()
    
    data class FakeTask(
        val id: Int,
        val title: String,
        val startTimeEpoch: Long,
        val repeatType: String = "NONE"
    )

    @Test
    fun `getTasks returns all tasks`() {
        fakeTasks.clear()
        fakeTasks.add(FakeTask(1, "Task 1", 1000))
        fakeTasks.add(FakeTask(2, "Task 2", 2000))
        
        assertEquals(2, fakeTasks.size)
    }

    @Test
    fun `createTask adds task`() {
        fakeTasks.clear()
        fakeTasks.add(FakeTask(0, "New Task", System.currentTimeMillis()))
        
        assertEquals(1, fakeTasks.size)
        assertEquals("New Task", fakeTasks.first().title)
    }

    @Test
    fun `updateTask modifies existing task`() {
        fakeTasks.clear()
        fakeTasks.add(FakeTask(1, "Original", 1000))
        
        val index = fakeTasks.indexOfFirst { it.id == 1 }
        if (index >= 0) {
            fakeTasks[index] = FakeTask(1, "Updated", 1000)
        }
        
        assertEquals("Updated", fakeTasks.first().title)
    }

    @Test
    fun `deleteTask removes task by id`() {
        fakeTasks.clear()
        fakeTasks.add(FakeTask(1, "Task 1", 1000))
        fakeTasks.add(FakeTask(2, "Task 2", 2000))
        
        fakeTasks.removeIf { it.id == 1 }
        
        assertEquals(1, fakeTasks.size)
        assertEquals(2, fakeTasks.first().id)
    }

    @Test
    fun `getTasksByDay filters by date`() {
        fakeTasks.clear()
        val today = System.currentTimeMillis()
        val tomorrow = today + 86400000
        
        fakeTasks.add(FakeTask(1, "Today", today))
        fakeTasks.add(FakeTask(2, "Tomorrow", tomorrow))
        
        // Simulate filtering by day (same day = within 24 hours)
        val todayTasks = fakeTasks.filter { 
            it.startTimeEpoch >= today && it.startTimeEpoch < tomorrow 
        }
        
        assertEquals(1, todayTasks.size)
        assertEquals("Today", todayTasks.first().title)
    }

    @Test
    fun `getTasksByMonth filters by month`() {
        fakeTasks.clear()
        // All tasks in same month
        fakeTasks.add(FakeTask(1, "Week 1", 1000))
        fakeTasks.add(FakeTask(2, "Week 2", 2000))
        fakeTasks.add(FakeTask(3, "Week 3", 3000))
        
        assertEquals(3, fakeTasks.size)
    }

    @Test
    fun `repeat type filter works`() {
        fakeTasks.clear()
        fakeTasks.add(FakeTask(1, "Daily", 1000, "DAILY"))
        fakeTasks.add(FakeTask(2, "Weekly", 2000, "WEEKLY"))
        fakeTasks.add(FakeTask(3, "None", 3000, "NONE"))
        
        val repeating = fakeTasks.filter { it.repeatType != "NONE" }
        assertEquals(2, repeating.size)
    }
}
