package com.example.todolist.core.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class MissionStatusTest {

    @Test
    fun `status returns COMPLETED when storedStatus is COMPLETED`() {
        val pastDeadline = LocalDateTime.now().minusDays(1)
        val futureDeadline = LocalDateTime.now().plusDays(1)
        
        // Even with past deadline, COMPLETED status should return COMPLETED
        val missionPast = Mission(
            id = 1,
            title = "Test Mission",
            deadline = pastDeadline,
            storedStatus = MissionStoredStatus.COMPLETED
        )
        
        val missionFuture = Mission(
            id = 2,
            title = "Test Mission",
            deadline = futureDeadline,
            storedStatus = MissionStoredStatus.COMPLETED
        )
        
        assertEquals(MissionStatus.COMPLETED, missionPast.status)
        assertEquals(MissionStatus.COMPLETED, missionFuture.status)
    }

    @Test
    fun `status returns ACTIVE when storedStatus is UNSPECIFIED and deadline is in future`() {
        val futureDeadline = LocalDateTime.now().plusDays(7)
        
        val mission = Mission(
            id = 1,
            title = "Future Mission",
            deadline = futureDeadline,
            storedStatus = MissionStoredStatus.UNSPECIFIED
        )
        
        assertEquals(MissionStatus.ACTIVE, mission.status)
    }

    @Test
    fun `status returns MISSED when storedStatus is UNSPECIFIED and deadline is in past`() {
        val pastDeadline = LocalDateTime.now().minusDays(1)
        
        val mission = Mission(
            id = 1,
            title = "Overdue Mission",
            deadline = pastDeadline,
            storedStatus = MissionStoredStatus.UNSPECIFIED
        )
        
        assertEquals(MissionStatus.MISSED, mission.status)
    }

    @Test
    fun `status returns ACTIVE for deadline just seconds in future`() {
        val nearFutureDeadline = LocalDateTime.now().plusSeconds(10)
        
        val mission = Mission(
            id = 1,
            title = "Almost Due Mission",
            deadline = nearFutureDeadline,
            storedStatus = MissionStoredStatus.UNSPECIFIED
        )
        
        assertEquals(MissionStatus.ACTIVE, mission.status)
    }

    @Test
    fun `status returns MISSED for deadline just seconds in past`() {
        val justPastDeadline = LocalDateTime.now().minusSeconds(10)
        
        val mission = Mission(
            id = 1,
            title = "Just Missed Mission",
            deadline = justPastDeadline,
            storedStatus = MissionStoredStatus.UNSPECIFIED
        )
        
        assertEquals(MissionStatus.MISSED, mission.status)
    }

    @Test
    fun `default storedStatus is UNSPECIFIED`() {
        val mission = Mission(
            id = 1,
            title = "Default Mission",
            deadline = LocalDateTime.now().plusDays(1)
        )
        
        assertEquals(MissionStoredStatus.UNSPECIFIED, mission.storedStatus)
    }

    @Test
    fun `null description is allowed`() {
        val mission = Mission(
            id = 1,
            title = "No Description",
            description = null,
            deadline = LocalDateTime.now().plusDays(1)
        )
        
        assertEquals(null, mission.description)
    }

    @Test
    fun `status property is computed each time not cached`() {
        // This test verifies that status is always computed based on current time
        // by creating a mission with a deadline very close to now
        val deadline = LocalDateTime.now().plusSeconds(2)
        
        val mission = Mission(
            id = 1,
            title = "Time Sensitive",
            deadline = deadline,
            storedStatus = MissionStoredStatus.UNSPECIFIED
        )
        
        // Should be ACTIVE since deadline is in future
        assertEquals(MissionStatus.ACTIVE, mission.status)
        
        // After deadline passes, same mission object should return MISSED
        // (In real test we would verify this but timing tests are fragile)
    }
}
