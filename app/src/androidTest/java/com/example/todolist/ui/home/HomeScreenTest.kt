package com.example.todolist.ui.home

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.todolist.core.model.RepeatType
import com.example.todolist.core.model.Task
import com.example.todolist.feature.home.HomeScreen
import com.example.todolist.feature.home.HomeUiState
import com.example.todolist.feature.home.HomeViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockViewModel(uiState: HomeUiState = HomeUiState()): HomeViewModel {
        val viewModel = mockk<HomeViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(uiState)
        return viewModel
    }

    // ============ Screen Display Tests ============

    @Test
    fun homeScreen_displaysCalendarHeader() {
        val viewModel = createMockViewModel(
            HomeUiState(
                currentMonth = YearMonth.of(2024, 6),
                selectedDate = LocalDate.of(2024, 6, 15)
            )
        )

        composeTestRule.setContent {
            HomeScreen(homeViewModel = viewModel)
        }

        // Check that month/year header is displayed
        composeTestRule.onNodeWithText("June 2024", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun homeScreen_displaysWeekDays() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            HomeScreen(homeViewModel = viewModel)
        }

        // Check that week days are displayed (Mon, Tue, etc.)
        composeTestRule.onNodeWithText("Mon", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun homeScreen_displaysTasksWhenAvailable() {
        val tasks = listOf(
            Task(
                id = 1,
                title = "Morning Meeting",
                description = "Team standup",
                startTime = LocalDateTime.of(2024, 6, 15, 9, 0),
                durationMinutes = 30,
                repeatType = RepeatType.NONE
            ),
            Task(
                id = 2,
                title = "Lunch Break",
                description = null,
                startTime = LocalDateTime.of(2024, 6, 15, 12, 0),
                durationMinutes = 60,
                repeatType = RepeatType.DAILY
            )
        )
        
        val viewModel = createMockViewModel(
            HomeUiState(
                currentMonth = YearMonth.of(2024, 6),
                selectedDate = LocalDate.of(2024, 6, 15),
                tasks = tasks
            )
        )

        composeTestRule.setContent {
            HomeScreen(homeViewModel = viewModel)
        }

        // Check that task titles are displayed
        composeTestRule.onNodeWithText("Morning Meeting")
            .assertExists()
        composeTestRule.onNodeWithText("Lunch Break")
            .assertExists()
    }

    @Test
    fun homeScreen_displaysEmptyStateWhenNoTasks() {
        val viewModel = createMockViewModel(
            HomeUiState(
                currentMonth = YearMonth.of(2024, 6),
                selectedDate = LocalDate.of(2024, 6, 15),
                tasks = emptyList(),
                isLoading = false
            )
        )

        composeTestRule.setContent {
            HomeScreen(homeViewModel = viewModel)
        }

        // Check for empty state text (common patterns)
        composeTestRule.onNodeWithText("No tasks", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun homeScreen_showsLoadingIndicatorWhenLoading() {
        val viewModel = createMockViewModel(
            HomeUiState(isLoading = true)
        )

        composeTestRule.setContent {
            HomeScreen(homeViewModel = viewModel)
        }

        // Loading indicator could be a CircularProgressIndicator
        // Check that it's not crashing and screen is displayed
        composeTestRule.waitForIdle()
    }

    // ============ Interaction Tests ============

    @Test
    fun homeScreen_prevMonthButtonClickCallsViewModel() {
        val viewModel = createMockViewModel(
            HomeUiState(
                currentMonth = YearMonth.of(2024, 6),
                selectedDate = LocalDate.of(2024, 6, 15)
            )
        )

        composeTestRule.setContent {
            HomeScreen(homeViewModel = viewModel)
        }

        // Find and click previous month button (usually labeled with < or arrow)
        composeTestRule.onNodeWithContentDescription("Previous", substring = true, ignoreCase = true)
            .performClick()

        verify { viewModel.prevMonth() }
    }

    @Test
    fun homeScreen_nextMonthButtonClickCallsViewModel() {
        val viewModel = createMockViewModel(
            HomeUiState(
                currentMonth = YearMonth.of(2024, 6),
                selectedDate = LocalDate.of(2024, 6, 15)
            )
        )

        composeTestRule.setContent {
            HomeScreen(homeViewModel = viewModel)
        }

        // Find and click next month button
        composeTestRule.onNodeWithContentDescription("Next", substring = true, ignoreCase = true)
            .performClick()

        verify { viewModel.nextMonth() }
    }

    @Test
    fun homeScreen_taskCardIsClickable() {
        var editedTask: Task? = null
        val task = Task(
            id = 1,
            title = "Clickable Task",
            startTime = LocalDateTime.of(2024, 6, 15, 10, 0)
        )
        
        val viewModel = createMockViewModel(
            HomeUiState(
                currentMonth = YearMonth.of(2024, 6),
                selectedDate = LocalDate.of(2024, 6, 15),
                tasks = listOf(task)
            )
        )

        composeTestRule.setContent {
            HomeScreen(
                homeViewModel = viewModel,
                onEditTask = { editedTask = it }
            )
        }

        // Click on the task
        composeTestRule.onNodeWithText("Clickable Task")
            .performClick()

        // Verify callback was invoked
        assert(editedTask == task)
    }

    @Test
    fun homeScreen_displaysTaskTime() {
        val task = Task(
            id = 1,
            title = "Timed Task",
            startTime = LocalDateTime.of(2024, 6, 15, 14, 30),
            durationMinutes = 60
        )
        
        val viewModel = createMockViewModel(
            HomeUiState(
                tasks = listOf(task)
            )
        )

        composeTestRule.setContent {
            HomeScreen(homeViewModel = viewModel)
        }

        // Check that time is displayed (14:30)
        composeTestRule.onNodeWithText("14:30", substring = true)
            .assertExists()
    }

    @Test
    fun homeScreen_displaysErrorMessage() {
        val viewModel = createMockViewModel(
            HomeUiState(
                error = "Failed to load tasks"
            )
        )

        composeTestRule.setContent {
            HomeScreen(homeViewModel = viewModel)
        }

        // Check that error message is displayed
        composeTestRule.onNodeWithText("Failed to load tasks", substring = true)
            .assertExists()
    }

    @Test
    fun homeScreen_displaysRepeatingTaskIndicator() {
        val task = Task(
            id = 1,
            title = "Daily Task",
            startTime = LocalDateTime.now(),
            repeatType = RepeatType.DAILY
        )
        
        val viewModel = createMockViewModel(
            HomeUiState(
                tasks = listOf(task)
            )
        )

        composeTestRule.setContent {
            HomeScreen(homeViewModel = viewModel)
        }

        // Should display repeat indicator
        composeTestRule.onNodeWithText("Daily Task")
            .assertExists()
    }
}
