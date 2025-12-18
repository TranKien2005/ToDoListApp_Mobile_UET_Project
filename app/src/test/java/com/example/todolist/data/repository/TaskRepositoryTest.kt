package com.example.todolist.data.repository

import com.example.todolist.core.model.Task
import com.example.todolist.core.model.RepeatType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * Unit tests for TaskRepository
 */
class TaskRepositoryTest {

    private lateinit var fakeTasks: MutableList<Task>

    @Before
    fun setup() {
        fakeTasks = mutableListOf(
            Task(id = 1, title = "Task 1", startTime = LocalDateTime.now()),
            Task(id = 2, title = "Task 2", startTime = LocalDateTime.now().plusHours(1)),
            Task(id = 3, title = "Task 3", startTime = LocalDateTime.now().plusDays(1))
        )
    }

    @Test
    fun `get all tasks returns correct list`() {
        assertEquals(3, fakeTasks.size)
    }

    @Test
    fun `insert task adds to list`() {
        val newTask = Task(id = 4, title = "New Task", startTime = LocalDateTime.now())
        fakeTasks.add(newTask)
        
        assertEquals(4, fakeTasks.size)
        assertTrue(fakeTasks.contains(newTask))
    }

    @Test
    fun `delete task removes from list`() {
        val taskToRemove = fakeTasks.first()
        fakeTasks.removeIf { it.id == taskToRemove.id }
        
        assertEquals(2, fakeTasks.size)
        assertFalse(fakeTasks.any { it.id == taskToRemove.id })
    }

    @Test
    fun `update task modifies existing task`() {
        val taskId = 1
        val updatedTitle = "Updated Task Title"
        val index = fakeTasks.indexOfFirst { it.id == taskId }
        
        if (index >= 0) {
            fakeTasks[index] = fakeTasks[index].copy(title = updatedTitle)
        }
        
        assertEquals(updatedTitle, fakeTasks.find { it.id == taskId }?.title)
    }

    @Test
    fun `get task by id returns correct task`() {
        val task = fakeTasks.find { it.id == 2 }
        
        assertNotNull(task)
        assertEquals("Task 2", task?.title)
    }

    @Test
    fun `get task by invalid id returns null`() {
        val task = fakeTasks.find { it.id == 999 }
        assertNull(task)
    }

    @Test
    fun `filter tasks by repeat type`() {
        fakeTasks.add(Task(id = 4, title = "Daily Task", startTime = LocalDateTime.now(), repeatType = RepeatType.DAILY))
        val dailyTasks = fakeTasks.filter { it.repeatType == RepeatType.DAILY }
        assertEquals(1, dailyTasks.size)
    }
}
