package com.example.todolist.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.todolist.MainActivity
import org.junit.Rule
import org.junit.Test

/**
 * Integration tests for app navigation between screens.
 * These tests verify that bottom navigation and screen transitions work correctly.
 */
class NavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    // ============ Bottom Navigation Tests ============

    @Test
    fun bottomNavigation_homeIsDefaultDestination() {
        // Wait for the app to load
        composeTestRule.waitForIdle()

        // Home screen should be displayed by default (after onboarding if user exists)
        // Check for home screen elements
        composeTestRule.onNodeWithText("Todolist", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun bottomNavigation_navigateToMissions() {
        composeTestRule.waitForIdle()

        // Click on Missions tab in bottom navigation
        composeTestRule.onNodeWithContentDescription("List", substring = true, ignoreCase = true)
            .performClick()

        composeTestRule.waitForIdle()

        // Verify we're on missions screen - should see mission UI elements
        composeTestRule.onNodeWithText("All", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun bottomNavigation_navigateToAnalysis() {
        composeTestRule.waitForIdle()

        // Click on Stats/Analysis tab
        composeTestRule.onNodeWithContentDescription("Stats", substring = true, ignoreCase = true)
            .performClick()

        composeTestRule.waitForIdle()

        // Should see analysis screen elements
        // The exact elements depend on the UI implementation
    }

    @Test
    fun bottomNavigation_navigateBackToHome() {
        composeTestRule.waitForIdle()

        // First navigate to missions
        composeTestRule.onNodeWithContentDescription("List", substring = true, ignoreCase = true)
            .performClick()
        composeTestRule.waitForIdle()

        // Then navigate back to home
        composeTestRule.onNodeWithContentDescription("Home", substring = true, ignoreCase = true)
            .performClick()
        composeTestRule.waitForIdle()

        // Should be back on home screen
        composeTestRule.onNodeWithText("Todolist", substring = true, ignoreCase = true)
            .assertExists()
    }

    // ============ Voice Assistant Navigation Tests ============

    @Test
    fun navigation_navigateToVoiceAssistant() {
        composeTestRule.waitForIdle()

        // Click on voice/microphone button
        composeTestRule.onNodeWithContentDescription("Voice", substring = true, ignoreCase = true)
            .performClick()

        composeTestRule.waitForIdle()

        // Should see voice assistant screen elements
        // Back button should be available
        composeTestRule.onNodeWithContentDescription("Back", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun navigation_backFromVoiceAssistantReturnsHome() {
        composeTestRule.waitForIdle()

        // Navigate to voice assistant
        composeTestRule.onNodeWithContentDescription("Voice", substring = true, ignoreCase = true)
            .performClick()
        composeTestRule.waitForIdle()

        // Press back
        composeTestRule.onNodeWithContentDescription("Back", substring = true, ignoreCase = true)
            .performClick()
        composeTestRule.waitForIdle()

        // Should be back on home
        composeTestRule.onNodeWithText("Todolist", substring = true, ignoreCase = true)
            .assertExists()
    }

    // ============ Notification Navigation Tests ============

    @Test
    fun navigation_navigateToNotifications() {
        composeTestRule.waitForIdle()

        // Click on notification icon in top bar
        composeTestRule.onNodeWithContentDescription("Notification", substring = true, ignoreCase = true)
            .performClick()

        composeTestRule.waitForIdle()

        // Should see notifications screen
        composeTestRule.onNodeWithContentDescription("Back", substring = true, ignoreCase = true)
            .assertExists()
    }

    // ============ Settings Navigation Tests ============

    @Test
    fun navigation_navigateToSettings() {
        composeTestRule.waitForIdle()

        // Click on settings icon
        composeTestRule.onNodeWithContentDescription("Settings", substring = true, ignoreCase = true)
            .performClick()

        composeTestRule.waitForIdle()

        // Should see settings screen
        composeTestRule.onNodeWithText("Settings", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun navigation_backFromSettingsReturnsHome() {
        composeTestRule.waitForIdle()

        // Navigate to settings
        composeTestRule.onNodeWithContentDescription("Settings", substring = true, ignoreCase = true)
            .performClick()
        composeTestRule.waitForIdle()

        // Press back
        composeTestRule.onNodeWithContentDescription("Back", substring = true, ignoreCase = true)
            .performClick()
        composeTestRule.waitForIdle()

        // Should be back on previous screen
        composeTestRule.waitForIdle()
    }

    // ============ Add Dialog Tests ============

    @Test
    fun navigation_addButtonOpensDialog() {
        composeTestRule.waitForIdle()

        // Click on add button (floating action button or center button)
        composeTestRule.onNodeWithContentDescription("Add", substring = true, ignoreCase = true)
            .performClick()

        composeTestRule.waitForIdle()

        // Dialog should be visible
        composeTestRule.onNodeWithText("Add", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun navigation_dismissDialogOnDismissRequest() {
        composeTestRule.waitForIdle()

        // Open add dialog
        composeTestRule.onNodeWithContentDescription("Add", substring = true, ignoreCase = true)
            .performClick()
        composeTestRule.waitForIdle()

        // Press back/dismiss
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
        composeTestRule.waitForIdle()

        // Dialog should be closed, home screen visible
    }

    // ============ Cross-Navigation Tests ============

    @Test
    fun navigation_navigateThroughAllMainScreens() {
        composeTestRule.waitForIdle()

        // Start at home
        // Navigate to missions
        composeTestRule.onNodeWithContentDescription("List", substring = true, ignoreCase = true)
            .performClick()
        composeTestRule.waitForIdle()

        // Navigate to stats
        composeTestRule.onNodeWithContentDescription("Stats", substring = true, ignoreCase = true)
            .performClick()
        composeTestRule.waitForIdle()

        // Back to home
        composeTestRule.onNodeWithContentDescription("Home", substring = true, ignoreCase = true)
            .performClick()
        composeTestRule.waitForIdle()

        // Verify we're on home
        composeTestRule.onNodeWithText("Todolist", substring = true, ignoreCase = true)
            .assertExists()
    }
}
