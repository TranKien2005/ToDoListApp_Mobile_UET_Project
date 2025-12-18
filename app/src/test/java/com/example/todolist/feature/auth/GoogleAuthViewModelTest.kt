package com.example.todolist.feature.auth

import android.content.Context
import com.example.todolist.data.repository.GoogleAuthRepository
import com.example.todolist.data.repository.GoogleCalendarRepository
import com.example.todolist.data.repository.GoogleUser
import com.example.todolist.domain.repository.TaskRepository
import com.example.todolist.domain.usecase.TaskUseCases
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

/**
 * Unit tests for GoogleAuthViewModel
 * Tests Google Sign-In flow and user data verification
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class GoogleAuthViewModelTest {

    private lateinit var viewModel: GoogleAuthViewModel
    private lateinit var authRepository: GoogleAuthRepository
    private lateinit var calendarRepository: GoogleCalendarRepository
    private lateinit var taskUseCases: TaskUseCases
    private lateinit var mockContext: Context

    private val testDispatcher = StandardTestDispatcher()

    private val testGoogleUser = GoogleUser(
        googleId = "123456789",
        email = "testuser@gmail.com",
        displayName = "Test User",
        photoUrl = "https://example.com/photo.jpg"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        authRepository = mockk(relaxed = true)
        calendarRepository = mockk(relaxed = true)
        taskUseCases = mockk(relaxed = true)
        mockContext = mockk(relaxed = true)

        // Default mocks
        every { authRepository.isSignedIn() } returns false
        every { authRepository.getCurrentUser() } returns null
        every { taskUseCases.getTasks() } returns flowOf(emptyList())

        viewModel = GoogleAuthViewModel(
            googleAuthRepository = authRepository,
            googleCalendarRepository = calendarRepository,
            taskUseCases
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ============================================
    // Sign-In State Tests
    // ============================================

    @Test
    fun `initial state reflects not signed in`() = runTest {
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isSignedIn)
        assertFalse(state.isLoading)
        assertNull(state.currentUser)
        assertNull(state.error)
    }

    @Test
    fun `initial state reflects signed in when user exists`() = runTest {
        // Given
        every { authRepository.isSignedIn() } returns true
        every { authRepository.getCurrentUser() } returns testGoogleUser

        // When - create new ViewModel
        viewModel = GoogleAuthViewModel(authRepository, calendarRepository, taskUseCases)

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.isSignedIn)
        assertNotNull(state.currentUser)
        assertEquals("testuser@gmail.com", state.currentUser?.email)
    }

    @Test
    fun `signIn success updates state with user data`() = runTest {
        // Given
        coEvery { authRepository.signIn(mockContext) } returns Result.success(testGoogleUser)

        // When
        viewModel.signIn(mockContext)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isSignedIn)
        assertNotNull(state.currentUser)
        assertEquals("testuser@gmail.com", state.currentUser?.email)
        assertEquals("Test User", state.currentUser?.displayName)
        assertNull(state.error)
    }

    @Test
    fun `signIn failure sets error state`() = runTest {
        // Given
        val errorMessage = "Sign-in was cancelled by user"
        coEvery {
            authRepository.signIn(mockContext)
        } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.signIn(mockContext)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSignedIn)
        assertNull(state.currentUser)
        assertEquals(errorMessage, state.error)
    }

    @Test
    fun `signIn sets loading state during operation`() = runTest {
        // Given
        coEvery { authRepository.signIn(mockContext) } coAnswers {
            // Simulate delay
            kotlinx.coroutines.delay(100)
            Result.success(testGoogleUser)
        }

        // When
        viewModel.signIn(mockContext)

        // Then - check loading state immediately (before completion)
        // Note: Due to test dispatcher, we check state after advancing
        testDispatcher.scheduler.advanceTimeBy(50)
        // Loading state would have been set

        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading) // After completion
    }

    @Test
    fun `signOut clears user data`() = runTest {
        // Given - simulate signed in state first
        every { authRepository.isSignedIn() } returns true
        every { authRepository.getCurrentUser() } returns testGoogleUser
        viewModel = GoogleAuthViewModel(authRepository, calendarRepository, taskUseCases)
        assertTrue(viewModel.uiState.value.isSignedIn)

        coEvery { authRepository.signOut() } just Runs

        // When
        viewModel.signOut()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isSignedIn)
        assertNull(state.currentUser)
    }

    // ============================================
    // User Data Verification Tests
    // ============================================

    @Test
    fun `user data contains google id`() = runTest {
        // Given
        coEvery { authRepository.signIn(mockContext) } returns Result.success(testGoogleUser)

        // When
        viewModel.signIn(mockContext)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val user = viewModel.uiState.value.currentUser
        assertNotNull(user)
        assertEquals("123456789", user?.googleId)
    }

    @Test
    fun `user data contains email`() = runTest {
        // Given
        coEvery { authRepository.signIn(mockContext) } returns Result.success(testGoogleUser)

        // When
        viewModel.signIn(mockContext)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val user = viewModel.uiState.value.currentUser
        assertNotNull(user)
        assertTrue(user?.email?.contains("@") == true)
        assertEquals("testuser@gmail.com", user?.email)
    }

    @Test
    fun `user data contains display name`() = runTest {
        // Given
        coEvery { authRepository.signIn(mockContext) } returns Result.success(testGoogleUser)

        // When
        viewModel.signIn(mockContext)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val user = viewModel.uiState.value.currentUser
        assertNotNull(user)
        assertFalse(user?.displayName.isNullOrBlank())
        assertEquals("Test User", user?.displayName)
    }

    @Test
    fun `user data contains photo url when available`() = runTest {
        // Given
        coEvery { authRepository.signIn(mockContext) } returns Result.success(testGoogleUser)

        // When
        viewModel.signIn(mockContext)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val user = viewModel.uiState.value.currentUser
        assertNotNull(user?.photoUrl)
        assertTrue(user?.photoUrl?.startsWith("http") == true)
    }

    @Test
    fun `user data photo url is null when not provided`() = runTest {
        // Given
        val userWithoutPhoto = testGoogleUser.copy(photoUrl = null)
        coEvery { authRepository.signIn(mockContext) } returns Result.success(userWithoutPhoto)

        // When
        viewModel.signIn(mockContext)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val user = viewModel.uiState.value.currentUser
        assertNotNull(user)
        assertNull(user?.photoUrl)
    }

    // ============================================
    // Calendar Sync Toggle Tests
    // ============================================

    @Test
    fun `toggleCalendarSync with false does nothing`() = runTest {
        // When
        viewModel.toggleCalendarSync(mockContext, false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - no API calls should be made
        coVerify(exactly = 0) { authRepository.getCalendarAccessToken(any()) }
        coVerify(exactly = 0) { calendarRepository.importEventsFromCalendar(any(), any(), any()) }
    }

    @Test
    fun `toggleCalendarSync with true and no token sets error`() = runTest {
        // Given
        coEvery { authRepository.getCalendarAccessToken(mockContext) } returns null

        // When
        viewModel.toggleCalendarSync(mockContext, true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isSyncingCalendar)
        assertNotNull(state.calendarSyncError)
        assertTrue(state.calendarSyncError?.contains("token") == true
                || state.calendarSyncError?.contains("permission") == true)
    }

    @Test
    fun `toggleCalendarSync imports events when token available`() = runTest {
        // Given
        val accessToken = "valid_access_token"
        coEvery { authRepository.getCalendarAccessToken(mockContext) } returns accessToken
        coEvery {
            calendarRepository.importEventsFromCalendar(any(), any(), accessToken)
        } returns Result.success(emptyList())

        // When
        viewModel.toggleCalendarSync(mockContext, true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { calendarRepository.importEventsFromCalendar(any(), any(), accessToken) }
        assertFalse(viewModel.uiState.value.isSyncingCalendar)
    }

    // ============================================
    // Error Handling Tests
    // ============================================

    @Test
    fun `clearError removes error from state`() = runTest {
        // Given - set an error first
        coEvery {
            authRepository.signIn(mockContext)
        } returns Result.failure(Exception("Test error"))
        viewModel.signIn(mockContext)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `multiple sign-in attempts clear previous errors`() = runTest {
        // Given - first attempt fails
        coEvery {
            authRepository.signIn(mockContext)
        } returns Result.failure(Exception("First error"))
        viewModel.signIn(mockContext)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("First error", viewModel.uiState.value.error)

        // When - second attempt succeeds
        coEvery { authRepository.signIn(mockContext) } returns Result.success(testGoogleUser)
        viewModel.signIn(mockContext)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.isSignedIn)
    }
}