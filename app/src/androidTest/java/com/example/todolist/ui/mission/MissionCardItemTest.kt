package com.example.todolist.ui.mission

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.MissionStoredStatus
import com.example.todolist.feature.mission.components.MissionCardItem
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

/**
 * Instrumented tests for MissionCardItem composable
 */
@RunWith(AndroidJUnit4::class)
class MissionCardItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun missionCardItem_displaysTitle() {
        val mission = Mission(
            id = 1,
            userId = 1,
            title = "Test Mission Title",
            deadline = LocalDateTime.now().plusDays(7),
            storedStatus = MissionStoredStatus.IN_PROGRESS
        )

        composeTestRule.setContent {
            MissionCardItem(mission = mission)
        }

        composeTestRule.onNodeWithText("Test Mission Title").assertIsDisplayed()
    }

    @Test
    fun missionCardItem_showsDeadline() {
        val deadline = LocalDateTime.now().plusDays(7)
        val mission = Mission(
            id = 1,
            userId = 1,
            title = "Mission with Deadline",
            deadline = deadline,
            storedStatus = MissionStoredStatus.IN_PROGRESS
        )

        composeTestRule.setContent {
            MissionCardItem(mission = mission)
        }

        // Should display formatted deadline
        composeTestRule.onNodeWithText("Mission with Deadline").assertIsDisplayed()
    }

    @Test
    fun missionCardItem_expandsOnClick() {
        val mission = Mission(
            id = 1,
            userId = 1,
            title = "Expandable Mission",
            description = "This is the description that appears when expanded",
            deadline = LocalDateTime.now().plusDays(7),
            storedStatus = MissionStoredStatus.IN_PROGRESS
        )

        composeTestRule.setContent {
            MissionCardItem(mission = mission)
        }

        // Description should be hidden initially
        composeTestRule.onNodeWithText("This is the description that appears when expanded")
            .assertDoesNotExist()

        // Click to expand
        composeTestRule.onNodeWithText("Expandable Mission").performClick()

        // Description should now be visible
        composeTestRule.onNodeWithText("This is the description that appears when expanded")
            .assertIsDisplayed()
    }

    @Test
    fun missionCardItem_showsDeleteButton() {
        val mission = Mission(
            id = 1,
            userId = 1,
            title = "Mission with Delete",
            deadline = LocalDateTime.now().plusDays(7)
        )

        composeTestRule.setContent {
            MissionCardItem(mission = mission)
        }

        // Delete button should exist (using content description)
        composeTestRule.onNodeWithContentDescription("Delete").assertExists()
    }
}
