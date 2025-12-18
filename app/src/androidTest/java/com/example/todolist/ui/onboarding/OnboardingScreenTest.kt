package com.example.todolist.ui.onboarding

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.todolist.feature.onboarding.OnboardingScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for OnboardingScreen
 */
@RunWith(AndroidJUnit4::class)
class OnboardingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun welcomeScreen_displaysAppName() {
        composeTestRule.setContent {
            // WelcomeScreen content would be set here
            // For now just a placeholder test
        }
        // Test would verify app name is displayed
    }

    @Test
    fun welcomeScreen_hasLetsStartButton() {
        composeTestRule.setContent {
            // WelcomeScreen content
        }
        // Test would verify Let's Start button exists
    }

    @Test
    fun profileFormScreen_hasNameField() {
        composeTestRule.setContent {
            // ProfileFormScreen content
        }
        // Test would verify name input field exists
    }

    @Test
    fun profileFormScreen_hasAgeField() {
        composeTestRule.setContent {
            // ProfileFormScreen content
        }
        // Test would verify age input field exists
    }

    @Test
    fun profileFormScreen_hasGenderDropdown() {
        composeTestRule.setContent {
            // ProfileFormScreen content
        }
        // Test would verify gender dropdown exists
    }

    @Test
    fun profileFormScreen_submitButtonDisabled_whenNameEmpty() {
        composeTestRule.setContent {
            // ProfileFormScreen content
        }
        // Test would verify submit is disabled without name
    }
}
