package com.example.todolist.feature.calendar

import android.content.Intent
import com.example.todolist.core.model.RepeatType
import com.example.todolist.core.model.Task
import com.example.todolist.data.repository.GoogleCalendarRepository
import com.example.todolist.data.repository.GoogleSignInRepository
import com.example.todolist.domain.repository.TaskRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Unit tests for CalendarSyncViewModel
 * Tests Google Sign-In state management and calendar sync operations
 * 
 * FIX: Injecting testDispatcher as ioDispatcher to ensure coroutines
 * run on the test scheduler and advanceUntilIdle() works properly.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class CalendarSyncViewModelTest {

    private lateinit var viewModel: CalendarSyncViewModel
    private lateinit var signInRepository: GoogleSignInRepository
    private lateinit var calendarRepository: GoogleCalendarRepository
    private lateinit var taskRepository: TaskRepository

    private val testDispatcher = StandardTestDispatcher()

    private val testTask = Task(
        id = 1,
        title = "Test Task",
        description = "Test",
        startTime = LocalDateTime.of(2025, 1, 15, 10, 0),
        durationMinutes = 60,
        repeatType = RepeatType.NONE,
        googleCalendarEventId = null
    )

    private val testAccessToken = "test_token_abc123"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        signInRepository = mockk()
        calendarRepository = mockk()
        taskRepository = mockk()

        // Default stubs
        every { signInRepository.getCurrentAccount() } returns null
        every { signInRepository.hasCalendarPermission() } returns false
        every { signInRepository.getSignInIntent() } returns mockk()
        coEvery { signInRepository.getAccessToken() } returns null
        coEvery { signInRepository.signOut() } returns Result.success(Unit)
        coEvery { signInRepository.handleSignInResult(any()) } returns Result.failure(Exception("Not stubbed"))
        
        every { taskRepository.getTasks() } returns flowOf(listOf(testTask))
        coEvery { taskRepository.saveTask(any()) } just Runs
        coEvery { taskRepository.deleteTask(any()) } just Runs
        
        coEvery { calendarRepository.syncTaskToCalendar(any(), any()) } returns Result.failure(Exception("Not stubbed"))
        coEvery { calendarRepository.updateEventInCalendar(any(), any()) } returns Result.failure(Exception("Not stubbed"))
        coEvery { calendarRepository.deleteEventFromCalendar(any(), any()) } returns Result.failure(Exception("Not stubbed"))
        coEvery { calendarRepository.importEventsFromCalendar(any(), any(), any()) } returns Result.failure(Exception("Not stubbed"))

        // KEY FIX: Inject testDispatcher as ioDispatcher for proper test synchronization
        viewModel = CalendarSyncViewModel(
            signInRepository = signInRepository,
            calendarRepository = calendarRepository,
            taskRepository = taskRepository,
            ioDispatcher = testDispatcher  // <-- This is the fix!
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    // ============================================
    // Initial State Tests
    // ============================================

    @Test
    fun `initial state is not signed in`() = runTest {
        val state = viewModel.uiState.value
        assertFalse(state.isSignedIn)
        assertFalse(state.hasCalendarPermission)
        assertNull(state.userEmail)
        assertFalse(state.isSyncing)
        assertNull(state.error)
        assertNull(state.lastSyncResult)
    }

    @Test
    fun `checkSignInStatus updates state when signed in`() = runTest {
        val mockAccount = mockk<GoogleSignInAccount>()
        every { mockAccount.email } returns "test@example.com"
        every { mockAccount.photoUrl } returns null
        every { signInRepository.getCurrentAccount() } returns mockAccount
        every { signInRepository.hasCalendarPermission() } returns true

        viewModel.checkSignInStatus()

        val state = viewModel.uiState.value
        assertTrue(state.isSignedIn)
        assertTrue(state.hasCalendarPermission)
        assertEquals("test@example.com", state.userEmail)
    }

    // ============================================
    // Sign-In/Sign-Out Tests
    // ============================================

    @Test
    fun `handleSignInResult updates state on successful sign-in`() = runTest {
        val mockIntent = mockk<Intent>()
        val mockAccount = mockk<GoogleSignInAccount>()
        every { mockAccount.email } returns "user@gmail.com"
        every { mockAccount.photoUrl } returns null
        coEvery { signInRepository.handleSignInResult(mockIntent) } returns Result.success(mockAccount)
        every { signInRepository.hasCalendarPermission() } returns true

        viewModel.handleSignInResult(mockIntent)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSignedIn)
        assertEquals("user@gmail.com", state.userEmail)
        assertNull(state.error)
    }

    @Test
    fun `handleSignInResult sets error on failure`() = runTest {
        val mockIntent = mockk<Intent>()
        coEvery { 
            signInRepository.handleSignInResult(mockIntent) 
        } returns Result.failure(Exception("Sign-in cancelled"))

        viewModel.handleSignInResult(mockIntent)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSignedIn)
        assertEquals("Sign-in cancelled", state.error)
    }

    @Test
    fun `signOut clears user state`() = runTest {
        // Setup signed-in state first
        val mockAccount = mockk<GoogleSignInAccount>()
        every { mockAccount.email } returns "test@example.com"
        every { mockAccount.photoUrl } returns null
        every { signInRepository.getCurrentAccount() } returns mockAccount
        every { signInRepository.hasCalendarPermission() } returns true
        viewModel.checkSignInStatus()
        assertTrue(viewModel.uiState.value.isSignedIn)

        coEvery { signInRepository.signOut() } returns Result.success(Unit)

        viewModel.signOut()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSignedIn)
        assertNull(state.userEmail)
    }

    // ============================================
    // syncTask Tests - SUCCESS cases
    // ============================================

    @Test
    fun `syncTask creates event and emits success message when task has no event id`() = runTest {
        val eventId = "created_event_123"
        coEvery { signInRepository.getAccessToken() } returns testAccessToken
        coEvery { 
            calendarRepository.syncTaskToCalendar(testTask, testAccessToken) 
        } returns Result.success(eventId)
        coEvery { taskRepository.saveTask(any()) } just Runs

        viewModel.syncTask(testTask)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("isSyncing should be false after sync", state.isSyncing)
        assertEquals("Task synced successfully", state.lastSyncResult)
        assertNull("error should be null on success", state.error)
        
        coVerify { calendarRepository.syncTaskToCalendar(testTask, testAccessToken) }
        coVerify { taskRepository.saveTask(match { it.googleCalendarEventId == eventId }) }
    }

    @Test
    fun `syncTask updates event and emits success message when task has existing event id`() = runTest {
        val taskWithEventId = testTask.copy(googleCalendarEventId = "existing_event")
        coEvery { signInRepository.getAccessToken() } returns testAccessToken
        coEvery { 
            calendarRepository.updateEventInCalendar(taskWithEventId, testAccessToken) 
        } returns Result.success(Unit)

        viewModel.syncTask(taskWithEventId)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("isSyncing should be false after sync", state.isSyncing)
        assertEquals("Task synced successfully", state.lastSyncResult)
        assertNull("error should be null on success", state.error)
        
        coVerify { calendarRepository.updateEventInCalendar(taskWithEventId, testAccessToken) }
    }

    // ============================================
    // syncTask Tests - ERROR cases
    // ============================================

    @Test
    fun `syncTask emits error when not authenticated (null token)`() = runTest {
        coEvery { signInRepository.getAccessToken() } returns null

        viewModel.syncTask(testTask)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("isSyncing should be false", state.isSyncing)
        assertEquals("Not authenticated", state.error)
        assertNull("lastSyncResult should be null on error", state.lastSyncResult)
    }

    @Test
    fun `syncTask emits error when calendar API fails`() = runTest {
        coEvery { signInRepository.getAccessToken() } returns testAccessToken
        coEvery { 
            calendarRepository.syncTaskToCalendar(testTask, testAccessToken) 
        } returns Result.failure(Exception("Network error"))

        viewModel.syncTask(testTask)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("isSyncing should be false after error", state.isSyncing)
        assertEquals("Network error", state.error)
        assertNull("lastSyncResult should be null on error", state.lastSyncResult)
    }

    @Test
    fun `syncTask emits generic error when exception has no message`() = runTest {
        coEvery { signInRepository.getAccessToken() } returns testAccessToken
        coEvery { 
            calendarRepository.syncTaskToCalendar(testTask, testAccessToken) 
        } returns Result.failure(Exception())

        viewModel.syncTask(testTask)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Sync failed", state.error)
    }

    // ============================================
    // syncAllTasks Tests
    // ============================================

    @Test
    fun `syncAllTasks syncs all local tasks and emits correct count`() = runTest {
        val tasks = listOf(
            testTask,
            testTask.copy(id = 2, title = "Task 2")
        )
        every { taskRepository.getTasks() } returns flowOf(tasks)
        coEvery { signInRepository.getAccessToken() } returns testAccessToken
        coEvery { 
            calendarRepository.syncTaskToCalendar(any(), testAccessToken) 
        } returns Result.success("event_id")
        coEvery { taskRepository.saveTask(any()) } just Runs

        viewModel.syncAllTasks()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("isSyncing should be false", state.isSyncing)
        assertTrue("Should report 2 synced tasks", state.lastSyncResult?.contains("Synced 2 tasks") == true)
        assertTrue("Should report 0 failed", state.lastSyncResult?.contains("(0 failed)") == true)
    }

    @Test
    fun `syncAllTasks reports correct failure count`() = runTest {
        val tasks = listOf(
            testTask,
            testTask.copy(id = 2, title = "Task 2")
        )
        every { taskRepository.getTasks() } returns flowOf(tasks)
        coEvery { signInRepository.getAccessToken() } returns testAccessToken
        coEvery { 
            calendarRepository.syncTaskToCalendar(match { it.id == 1 }, testAccessToken) 
        } returns Result.success("event_1")
        coEvery { 
            calendarRepository.syncTaskToCalendar(match { it.id == 2 }, testAccessToken) 
        } returns Result.failure(Exception("API error"))
        coEvery { taskRepository.saveTask(any()) } just Runs

        viewModel.syncAllTasks()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Should report 1 synced", state.lastSyncResult?.contains("Synced 1 tasks") == true)
        assertTrue("Should report 1 failed", state.lastSyncResult?.contains("(1 failed)") == true)
    }

    // ============================================
    // deleteTaskFromCalendar Tests
    // ============================================

    @Test
    fun `deleteTaskFromCalendar removes event from calendar`() = runTest {
        val taskWithEvent = testTask.copy(googleCalendarEventId = "event_to_delete")
        coEvery { signInRepository.getAccessToken() } returns testAccessToken
        coEvery { 
            calendarRepository.deleteEventFromCalendar("event_to_delete", testAccessToken) 
        } returns Result.success(Unit)

        viewModel.deleteTaskFromCalendar(taskWithEvent)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { calendarRepository.deleteEventFromCalendar("event_to_delete", testAccessToken) }
    }

    @Test
    fun `deleteTaskFromCalendar does nothing if task has no event id`() = runTest {
        val taskWithoutEvent = testTask.copy(googleCalendarEventId = null)

        viewModel.deleteTaskFromCalendar(taskWithoutEvent)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { calendarRepository.deleteEventFromCalendar(any(), any()) }
    }

    // ============================================
    // importFromCalendar Tests
    // ============================================

    @Test
    fun `importFromCalendar imports events and emits success message`() = runTest {
        val startDate = LocalDate.of(2025, 1, 1)
        val endDate = LocalDate.of(2025, 1, 31)
        val importedTasks = listOf(
            testTask.copy(id = 0, title = "Imported Event", googleCalendarEventId = "imp_1")
        )
        
        coEvery { signInRepository.getAccessToken() } returns testAccessToken
        coEvery { 
            calendarRepository.importEventsFromCalendar(startDate, endDate, testAccessToken) 
        } returns Result.success(importedTasks)
        coEvery { taskRepository.saveTask(any()) } just Runs

        viewModel.importFromCalendar(startDate, endDate)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("isSyncing should be false", state.isSyncing)
        assertTrue("Should report 1 imported event", state.lastSyncResult?.contains("Imported 1 events") == true)
        coVerify { taskRepository.saveTask(any()) }
    }

    @Test
    fun `importFromCalendar emits error when API fails`() = runTest {
        val startDate = LocalDate.of(2025, 1, 1)
        val endDate = LocalDate.of(2025, 1, 31)
        
        coEvery { signInRepository.getAccessToken() } returns testAccessToken
        coEvery { 
            calendarRepository.importEventsFromCalendar(startDate, endDate, testAccessToken) 
        } returns Result.failure(Exception("Calendar access denied"))

        viewModel.importFromCalendar(startDate, endDate)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("isSyncing should be false", state.isSyncing)
        assertEquals("Calendar access denied", state.error)
    }

    // ============================================
    // Error Clearing Tests
    // ============================================

    @Test
    fun `clearError clears error state`() = runTest {
        // Set error state via syncTask with null token
        coEvery { signInRepository.getAccessToken() } returns null
        viewModel.syncTask(testTask)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull("Error should be set", viewModel.uiState.value.error)

        viewModel.clearError()

        assertNull("Error should be cleared", viewModel.uiState.value.error)
    }

    @Test
    fun `clearSyncResult clears sync result message`() = runTest {
        // Set sync result via successful syncTask
        coEvery { signInRepository.getAccessToken() } returns testAccessToken
        coEvery { 
            calendarRepository.syncTaskToCalendar(testTask, testAccessToken) 
        } returns Result.success("event_id")
        coEvery { taskRepository.saveTask(any()) } just Runs
        
        viewModel.syncTask(testTask)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull("lastSyncResult should be set", viewModel.uiState.value.lastSyncResult)

        viewModel.clearSyncResult()

        assertNull("lastSyncResult should be cleared", viewModel.uiState.value.lastSyncResult)
    }
}
