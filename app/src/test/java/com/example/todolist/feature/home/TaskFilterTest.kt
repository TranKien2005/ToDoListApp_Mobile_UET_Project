package com.example.todolist.feature.home

import com.example.todolist.core.model.Task
import com.example.todolist.core.model.RepeatType
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Unit tests for task filtering logic
 */
class TaskFilterTest {

    private val today = LocalDate.now()
    private val now = LocalDateTime.now()
    
    private val sampleTasks = listOf(
        Task(id = 1, title = "Task Today 1", startTime = now.withHour(9)),
        Task(id = 2, title = "Task Today 2", startTime = now.withHour(14)),
        Task(id = 3, title = "Task Tomorrow", startTime = now.plusDays(1)),
        Task(id = 4, title = "Task Yesterday", startTime = now.minusDays(1)),
        Task(id = 5, title = "Daily Task", startTime = now, repeatType = RepeatType.DAILY)
    )

    @Test
    fun `filter tasks by date`() {
        val todayTasks = sampleTasks.filter { it.startTime.toLocalDate() == today }
        assertTrue(todayTasks.size >= 3) // 3 tasks scheduled for today
    }

    @Test
    fun `filter future tasks`() {
        val futureTasks = sampleTasks.filter { it.startTime.toLocalDate().isAfter(today) }
        assertEquals(1, futureTasks.size)
        assertEquals("Task Tomorrow", futureTasks.first().title)
    }

    @Test
    fun `filter past tasks`() {
        val pastTasks = sampleTasks.filter { it.startTime.toLocalDate().isBefore(today) }
        assertEquals(1, pastTasks.size)
        assertEquals("Task Yesterday", pastTasks.first().title)
    }

    @Test
    fun `filter repeating tasks`() {
        val repeating = sampleTasks.filter { it.repeatType != RepeatType.NONE }
        assertEquals(1, repeating.size)
        assertEquals(RepeatType.DAILY, repeating.first().repeatType)
    }

    @Test
    fun `filter non-repeating tasks`() {
        val nonRepeating = sampleTasks.filter { it.repeatType == RepeatType.NONE }
        assertEquals(4, nonRepeating.size)
    }

    @Test
    fun `sort tasks by start time`() {
        val sorted = sampleTasks.sortedBy { it.startTime }
        
        assertTrue(sorted[0].startTime <= sorted[1].startTime)
    }

    @Test
    fun `group tasks by date`() {
        val grouped = sampleTasks.groupBy { it.startTime.toLocalDate() }
        
        assertTrue(grouped.containsKey(today))
        assertTrue(grouped.containsKey(today.plusDays(1)))
        assertTrue(grouped.containsKey(today.minusDays(1)))
    }

    @Test
    fun `filter tasks with duration`() {
        val tasksWithDuration = listOf(
            Task(id = 1, title = "With Duration", startTime = now, durationMinutes = 60),
            Task(id = 2, title = "No Duration", startTime = now)
        )

        val withDuration = tasksWithDuration.filter { it.durationMinutes != null }
        assertEquals(1, withDuration.size)
    }

    @Test
    fun `filter tasks by repeat type`() {
        val dailyTasks = sampleTasks.filter { it.repeatType == RepeatType.DAILY }
        assertEquals(1, dailyTasks.size)
    }

    @Test
    fun `count repeat types`() {
        val repeatCounts = sampleTasks.groupBy { it.repeatType }.mapValues { it.value.size }
        
        assertEquals(4, repeatCounts[RepeatType.NONE])
        assertEquals(1, repeatCounts[RepeatType.DAILY])
    }
}
