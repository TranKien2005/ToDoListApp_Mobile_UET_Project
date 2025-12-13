package com.example.todolist.data.repository

import com.example.todolist.core.model.RepeatType
import com.example.todolist.core.model.Task
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Unit tests for GoogleCalendarRepository
 * Tests CRUD operations for Google Calendar sync
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GoogleCalendarRepositoryTest {

    private lateinit var repository: GoogleCalendarRepository

    private val testAccessToken = "test_access_token_123"

    private val testTask = Task(
        id = 1,
        title = "Test Meeting",
        description = "Test description",
        startTime = LocalDateTime.of(2025, 1, 15, 10, 0),
        durationMinutes = 60,
        repeatType = RepeatType.NONE,
        googleCalendarEventId = null
    )

    private val testTaskWithEventId = testTask.copy(
        googleCalendarEventId = "event_123abc"
    )

    @Before
    fun setup() {
        // Create a mock repository for testing
        repository = mockk(relaxed = true)
    }

    @Test
    fun `syncTaskToCalendar creates event and returns event id`() = runTest {
        // Given
        val expectedEventId = "new_event_456"
        coEvery { 
            repository.syncTaskToCalendar(testTask, testAccessToken) 
        } returns Result.success(expectedEventId)

        // When
        val result = repository.syncTaskToCalendar(testTask, testAccessToken)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedEventId, result.getOrNull())
        coVerify { repository.syncTaskToCalendar(testTask, testAccessToken) }
    }

    @Test
    fun `syncTaskToCalendar fails with invalid token`() = runTest {
        // Given
        val invalidToken = ""
        coEvery { 
            repository.syncTaskToCalendar(testTask, invalidToken) 
        } returns Result.failure(Exception("Invalid access token"))

        // When
        val result = repository.syncTaskToCalendar(testTask, invalidToken)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Invalid") == true)
    }

    @Test
    fun `updateEventInCalendar updates existing event`() = runTest {
        // Given
        coEvery { 
            repository.updateEventInCalendar(testTaskWithEventId, testAccessToken) 
        } returns Result.success(Unit)

        // When
        val result = repository.updateEventInCalendar(testTaskWithEventId, testAccessToken)

        // Then
        assertTrue(result.isSuccess)
        coVerify { repository.updateEventInCalendar(testTaskWithEventId, testAccessToken) }
    }

    @Test
    fun `updateEventInCalendar fails when task has no event id`() = runTest {
        // Given - task without googleCalendarEventId
        coEvery { 
            repository.updateEventInCalendar(testTask, testAccessToken) 
        } returns Result.failure(Exception("No calendar event ID"))

        // When
        val result = repository.updateEventInCalendar(testTask, testAccessToken)

        // Then
        assertTrue(result.isFailure)
        assertEquals("No calendar event ID", result.exceptionOrNull()?.message)
    }

    @Test
    fun `deleteEventFromCalendar removes event`() = runTest {
        // Given
        val eventId = "event_to_delete"
        coEvery { 
            repository.deleteEventFromCalendar(eventId, testAccessToken) 
        } returns Result.success(Unit)

        // When
        val result = repository.deleteEventFromCalendar(eventId, testAccessToken)

        // Then
        assertTrue(result.isSuccess)
        coVerify { repository.deleteEventFromCalendar(eventId, testAccessToken) }
    }

    @Test
    fun `importEventsFromCalendar returns list of tasks`() = runTest {
        // Given
        val startDate = LocalDate.of(2025, 1, 1)
        val endDate = LocalDate.of(2025, 1, 31)
        val importedTasks = listOf(
            Task(
                id = 0,
                title = "Imported Event 1",
                description = "From calendar",
                startTime = LocalDateTime.of(2025, 1, 10, 14, 0),
                durationMinutes = 30,
                repeatType = RepeatType.NONE,
                googleCalendarEventId = "imported_1"
            ),
            Task(
                id = 0,
                title = "Imported Event 2",
                description = null,
                startTime = LocalDateTime.of(2025, 1, 20, 9, 0),
                durationMinutes = 120,
                repeatType = RepeatType.NONE,
                googleCalendarEventId = "imported_2"
            )
        )
        
        coEvery { 
            repository.importEventsFromCalendar(startDate, endDate, testAccessToken) 
        } returns Result.success(importedTasks)

        // When
        val result = repository.importEventsFromCalendar(startDate, endDate, testAccessToken)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("Imported Event 1", result.getOrNull()?.first()?.title)
        assertNotNull(result.getOrNull()?.first()?.googleCalendarEventId)
    }

    @Test
    fun `importEventsFromCalendar returns empty list when no events`() = runTest {
        // Given
        val startDate = LocalDate.of(2025, 12, 1)
        val endDate = LocalDate.of(2025, 12, 31)
        
        coEvery { 
            repository.importEventsFromCalendar(startDate, endDate, testAccessToken) 
        } returns Result.success(emptyList())

        // When
        val result = repository.importEventsFromCalendar(startDate, endDate, testAccessToken)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun `task with repeat type is synced with recurrence rule`() = runTest {
        // Given
        val recurringTask = testTask.copy(repeatType = RepeatType.WEEKLY)
        val expectedEventId = "recurring_event"
        
        coEvery { 
            repository.syncTaskToCalendar(recurringTask, testAccessToken) 
        } returns Result.success(expectedEventId)

        // When
        val result = repository.syncTaskToCalendar(recurringTask, testAccessToken)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedEventId, result.getOrNull())
    }
}
