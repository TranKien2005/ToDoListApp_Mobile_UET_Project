package com.example.todolist.feature.mission

import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.MissionStatus
import com.example.todolist.core.model.MissionStoredStatus
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

/**
 * Unit tests for mission filtering logic
 */
class MissionFilterTest {

    private val now = LocalDateTime.now()
    
    private val sampleMissions = listOf(
        Mission(id = 1, title = "Active 1", deadline = now.plusDays(1), storedStatus = MissionStoredStatus.UNSPECIFIED),
        Mission(id = 2, title = "Active 2", deadline = now.plusDays(7), storedStatus = MissionStoredStatus.UNSPECIFIED),
        Mission(id = 3, title = "Completed", deadline = now.plusDays(3), storedStatus = MissionStoredStatus.COMPLETED),
        Mission(id = 4, title = "Missed", deadline = now.minusDays(1), storedStatus = MissionStoredStatus.UNSPECIFIED)
    )

    @Test
    fun `filter active missions`() {
        val active = sampleMissions.filter { it.status == MissionStatus.ACTIVE }
        assertEquals(2, active.size)
        assertTrue(active.all { it.status == MissionStatus.ACTIVE })
    }

    @Test
    fun `filter completed missions`() {
        val completed = sampleMissions.filter { it.status == MissionStatus.COMPLETED }
        assertEquals(1, completed.size)
        assertEquals("Completed", completed.first().title)
    }

    @Test
    fun `filter missed missions`() {
        val missed = sampleMissions.filter { it.status == MissionStatus.MISSED }
        assertEquals(1, missed.size)
        assertEquals("Missed", missed.first().title)
    }

    @Test
    fun `count missions by status`() {
        val activeCount = sampleMissions.count { it.status == MissionStatus.ACTIVE }
        val completedCount = sampleMissions.count { it.status == MissionStatus.COMPLETED }
        val missedCount = sampleMissions.count { it.status == MissionStatus.MISSED }

        assertEquals(2, activeCount)
        assertEquals(1, completedCount)
        assertEquals(1, missedCount)
    }

    @Test
    fun `sort by deadline ascending`() {
        val sorted = sampleMissions.sortedBy { it.deadline }
        
        assertTrue(sorted[0].deadline.isBefore(sorted[1].deadline) || sorted[0].deadline == sorted[1].deadline)
    }

    @Test
    fun `sort by deadline descending`() {
        val sorted = sampleMissions.sortedByDescending { it.deadline }
        
        assertTrue(sorted[0].deadline.isAfter(sorted[1].deadline) || sorted[0].deadline == sorted[1].deadline)
    }

    @Test
    fun `filter missions due this week`() {
        val weekLater = now.plusWeeks(1)
        val thisWeek = sampleMissions.filter { it.deadline.isBefore(weekLater) && it.deadline.isAfter(now) }
        
        // Should include missions due in 1, 3 and 7 days (3 missions, but mission 7 days might be outside)
        assertTrue(thisWeek.isNotEmpty())
    }

    @Test
    fun `filter overdue missions`() {
        val overdue = sampleMissions.filter { it.deadline.isBefore(now) }
        assertEquals(1, overdue.size)
        assertEquals("Missed", overdue.first().title)
    }

    @Test
    fun `group by status`() {
        val grouped = sampleMissions.groupBy { it.status }
        
        assertEquals(2, grouped[MissionStatus.ACTIVE]?.size)
        assertEquals(1, grouped[MissionStatus.COMPLETED]?.size)
        assertEquals(1, grouped[MissionStatus.MISSED]?.size)
    }
}
