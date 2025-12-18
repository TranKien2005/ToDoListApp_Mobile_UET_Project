package com.example.todolist.ui.home

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.todolist.core.model.Task
import com.example.todolist.core.model.RepeatType
import com.example.todolist.feature.home.components.TaskCardItem
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

/**
 * Instrumented tests for TaskCardItem composable
 */
@RunWith(AndroidJUnit4::class)
class TaskCardItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun taskCardItem_displaysTitle() {
        val task = Task(
            id = 1,
            userId = 1,
            title = "Test Task Title",
            startTime = LocalDateTime.now()
        )

        composeTestRule.setContent {
            TaskCardItem(task = task)
        }

        composeTestRule.onNodeWithText("Test Task Title").assertIsDisplayed()
    }

    @Test
    fun taskCardItem_showsTime() {
        val startTime = LocalDateTime.of(2024, 12, 18, 10, 30)
        val task = Task(
            id = 1,
            userId = 1,
            title = "Timed Task",
            startTime = startTime
        )

        composeTestRule.setContent {
            TaskCardItem(task = task)
        }

        composeTestRule.onNodeWithText("10:30").assertIsDisplayed()
    }

    @Test
    fun taskCardItem_showsRepeatIndicator_whenDaily() {
        val task = Task(
            id = 1,
            userId = 1,
            title = "Daily Task",
            startTime = LocalDateTime.now(),
            repeatType = RepeatType.DAILY
        )

        composeTestRule.setContent {
            TaskCardItem(task = task)
        }

        // Should show repeat indicator
        composeTestRule.onNodeWithText("Daily").assertIsDisplayed()
    }

    @Test
    fun taskCardItem_expandsOnClick() {
        val task = Task(
            id = 1,
            userId = 1,
            title = "Expandable Task",
            description = "Hidden description that shows on expand",
            startTime = LocalDateTime.now()
        )

        composeTestRule.setContent {
            TaskCardItem(task = task)
        }

        // Description hidden initially
        composeTestRule.onNodeWithText("Hidden description that shows on expand")
            .assertDoesNotExist()

        // Click to expand
        composeTestRule.onNodeWithText("Expandable Task").performClick()

        // Description visible after click
        composeTestRule.onNodeWithText("Hidden description that shows on expand")
            .assertIsDisplayed()
    }

    @Test
    fun taskCardItem_showsDuration_whenSet() {
        val task = Task(
            id = 1,
            userId = 1,
            title = "Task with Duration",
            startTime = LocalDateTime.of(2024, 12, 18, 10, 0),
            durationMinutes = 60
        )

        composeTestRule.setContent {
            TaskCardItem(task = task)
        }

        // Should show time range
        composeTestRule.onNodeWithText("10:00 - 11:00").assertIsDisplayed()
    }
}
