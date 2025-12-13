package com.example.todolist.ui.mission

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.MissionStoredStatus
import com.example.todolist.domain.usecase.StatsGranularity
import com.example.todolist.feature.mission.MissionScreen
import com.example.todolist.feature.mission.MissionStatusFilter
import com.example.todolist.feature.mission.MissionTag
import com.example.todolist.feature.mission.MissionUiState
import com.example.todolist.feature.mission.MissionViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class MissionScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockViewModel(uiState: MissionUiState = MissionUiState()): MissionViewModel {
        val viewModel = mockk<MissionViewModel>(relaxed = true)
        every { viewModel.uiState } returns MutableStateFlow(uiState)
        return viewModel
    }

    // ============ Screen Display Tests ============

    @Test
    fun missionScreen_displaysMissionsWhenAvailable() {
        val missions = listOf(
            Mission(
                id = 1,
                title = "Complete Project",
                description = "Finish all features",
                deadline = LocalDateTime.now().plusDays(3),
                storedStatus = MissionStoredStatus.UNSPECIFIED
            ),
            Mission(
                id = 2,
                title = "Review Code",
                description = null,
                deadline = LocalDateTime.now().plusDays(7),
                storedStatus = MissionStoredStatus.COMPLETED
            )
        )

        val viewModel = createMockViewModel(
            MissionUiState(
                missions = missions,
                isLoading = false
            )
        )

        composeTestRule.setContent {
            MissionScreen(missionViewModel = viewModel)
        }

        // Check that mission titles are displayed
        composeTestRule.onNodeWithText("Complete Project")
            .assertExists()
        composeTestRule.onNodeWithText("Review Code")
            .assertExists()
    }

    @Test
    fun missionScreen_displaysEmptyStateWhenNoMissions() {
        val viewModel = createMockViewModel(
            MissionUiState(
                missions = emptyList(),
                isLoading = false
            )
        )

        composeTestRule.setContent {
            MissionScreen(missionViewModel = viewModel)
        }

        // Check for empty state
        composeTestRule.onNodeWithText("No mission", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun missionScreen_displaysFilterChips() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            MissionScreen(missionViewModel = viewModel)
        }

        // Check that filter chips are displayed
        composeTestRule.onNodeWithText("All", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun missionScreen_showsLoadingState() {
        val viewModel = createMockViewModel(
            MissionUiState(isLoading = true)
        )

        composeTestRule.setContent {
            MissionScreen(missionViewModel = viewModel)
        }

        // Check screen renders without crashing during loading
        composeTestRule.waitForIdle()
    }

    // ============ Mission Card Tests ============

    @Test
    fun missionScreen_displaysDeadline() {
        val tomorrow = LocalDateTime.now().plusDays(1)
        val mission = Mission(
            id = 1,
            title = "Mission with Deadline",
            deadline = tomorrow,
            storedStatus = MissionStoredStatus.UNSPECIFIED
        )

        val viewModel = createMockViewModel(
            MissionUiState(missions = listOf(mission))
        )

        composeTestRule.setContent {
            MissionScreen(missionViewModel = viewModel)
        }

        // Check mission is displayed
        composeTestRule.onNodeWithText("Mission with Deadline")
            .assertExists()
    }

    @Test
    fun missionScreen_displaysMissionDescription() {
        val mission = Mission(
            id = 1,
            title = "Descriptive Mission",
            description = "This is a detailed description",
            deadline = LocalDateTime.now().plusDays(5),
            storedStatus = MissionStoredStatus.UNSPECIFIED
        )

        val viewModel = createMockViewModel(
            MissionUiState(missions = listOf(mission))
        )

        composeTestRule.setContent {
            MissionScreen(missionViewModel = viewModel)
        }

        // Check description is visible
        composeTestRule.onNodeWithText("This is a detailed description", substring = true)
            .assertExists()
    }

    // ============ Interaction Tests ============

    @Test
    fun missionScreen_tagChipClickCallsViewModel() {
        val viewModel = createMockViewModel(
            MissionUiState(selectedTag = MissionTag.ALL)
        )

        composeTestRule.setContent {
            MissionScreen(missionViewModel = viewModel)
        }

        // Click on "Today" filter chip
        composeTestRule.onNodeWithText("Today", substring = true, ignoreCase = true)
            .performClick()

        verify { viewModel.selectTag(MissionTag.TODAY) }
    }

    @Test
    fun missionScreen_statusFilterClickCallsViewModel() {
        val viewModel = createMockViewModel(
            MissionUiState(statusFilter = MissionStatusFilter.ALL)
        )

        composeTestRule.setContent {
            MissionScreen(missionViewModel = viewModel)
        }

        // Click on "Completed" filter
        composeTestRule.onNodeWithText("Completed", substring = true, ignoreCase = true)
            .performClick()

        verify { viewModel.setStatusFilter(MissionStatusFilter.COMPLETED) }
    }

    @Test
    fun missionScreen_missionCardClickTriggersEdit() {
        var editedMission: Mission? = null
        val mission = Mission(
            id = 42,
            title = "Editable Mission",
            deadline = LocalDateTime.now().plusDays(1)
        )

        val viewModel = createMockViewModel(
            MissionUiState(missions = listOf(mission))
        )

        composeTestRule.setContent {
            MissionScreen(
                missionViewModel = viewModel,
                onEditMission = { editedMission = it }
            )
        }

        // Click on the mission card
        composeTestRule.onNodeWithText("Editable Mission")
            .performClick()

        // Verify callback was invoked
        assert(editedMission?.id == 42)
    }

    @Test
    fun missionScreen_toggleCompletionCallsViewModel() {
        val mission = Mission(
            id = 10,
            title = "Completable Mission",
            deadline = LocalDateTime.now().plusDays(1),
            storedStatus = MissionStoredStatus.UNSPECIFIED
        )

        val viewModel = createMockViewModel(
            MissionUiState(missions = listOf(mission))
        )

        composeTestRule.setContent {
            MissionScreen(missionViewModel = viewModel)
        }

        // Find and click the completion toggle (checkbox or similar)
        composeTestRule.onNodeWithContentDescription("toggle", substring = true, ignoreCase = true)
            .performClick()

        verify { viewModel.toggleMissionCompleted(10) }
    }

    @Test
    fun missionScreen_deleteMissionCallsViewModel() {
        val mission = Mission(
            id = 20,
            title = "Deletable Mission",
            deadline = LocalDateTime.now().plusDays(1)
        )

        val viewModel = createMockViewModel(
            MissionUiState(missions = listOf(mission))
        )

        composeTestRule.setContent {
            MissionScreen(missionViewModel = viewModel)
        }

        // Find and click delete button (if visible)
        composeTestRule.onNodeWithContentDescription("delete", substring = true, ignoreCase = true)
            .performClick()

        verify { viewModel.deleteMission(20) }
    }

    // ============ Navigation Tests ============

    @Test
    fun missionScreen_prevButtonCallsViewModel() {
        val viewModel = createMockViewModel(
            MissionUiState(
                referenceDate = LocalDate.of(2024, 6, 15),
                granularity = StatsGranularity.WEEK_OF_MONTH
            )
        )

        composeTestRule.setContent {
            MissionScreen(missionViewModel = viewModel)
        }

        // Click previous navigation button
        composeTestRule.onNodeWithContentDescription("Previous", substring = true, ignoreCase = true)
            .performClick()

        verify { viewModel.prev() }
    }

    @Test
    fun missionScreen_nextButtonCallsViewModel() {
        val viewModel = createMockViewModel(
            MissionUiState(
                referenceDate = LocalDate.of(2024, 6, 15),
                granularity = StatsGranularity.WEEK_OF_MONTH
            )
        )

        composeTestRule.setContent {
            MissionScreen(missionViewModel = viewModel)
        }

        // Click next navigation button
        composeTestRule.onNodeWithContentDescription("Next", substring = true, ignoreCase = true)
            .performClick()

        verify { viewModel.next() }
    }

    // ============ Status Display Tests ============

    @Test
    fun missionScreen_displaysDifferentMissionStatuses() {
        val missions = listOf(
            Mission(
                id = 1,
                title = "Active Mission",
                deadline = LocalDateTime.now().plusDays(5),
                storedStatus = MissionStoredStatus.UNSPECIFIED
            ),
            Mission(
                id = 2,
                title = "Completed Mission",
                deadline = LocalDateTime.now().plusDays(3),
                storedStatus = MissionStoredStatus.COMPLETED
            ),
            Mission(
                id = 3,
                title = "Missed Mission",
                deadline = LocalDateTime.now().minusDays(1),
                storedStatus = MissionStoredStatus.UNSPECIFIED
            )
        )

        val viewModel = createMockViewModel(
            MissionUiState(missions = missions)
        )

        composeTestRule.setContent {
            MissionScreen(missionViewModel = viewModel)
        }

        // All missions should be visible
        composeTestRule.onNodeWithText("Active Mission").assertExists()
        composeTestRule.onNodeWithText("Completed Mission").assertExists()
        composeTestRule.onNodeWithText("Missed Mission").assertExists()
    }
}
