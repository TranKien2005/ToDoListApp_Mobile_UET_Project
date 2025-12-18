package com.example.todolist.feature.settings

import com.example.todolist.core.model.Settings
import com.example.todolist.core.model.AppLanguage
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Settings model
 */
class SettingsTest {

    @Test
    fun `default settings has correct values`() {
        val settings = Settings()

        assertEquals(AppLanguage.ENGLISH, settings.language)
        assertEquals(15, settings.taskReminderMinutes)
        assertTrue(settings.notifyDailyMissions)
    }

    @Test
    fun `settings can be updated with custom values`() {
        val customSettings = Settings(
            language = AppLanguage.VIETNAMESE,
            taskReminderMinutes = 30,
            notifyDailyMissions = false
        )

        assertEquals(AppLanguage.VIETNAMESE, customSettings.language)
        assertEquals(30, customSettings.taskReminderMinutes)
        assertFalse(customSettings.notifyDailyMissions)
    }

    @Test
    fun `settings copy works correctly`() {
        val original = Settings()
        val modified = original.copy(language = AppLanguage.VIETNAMESE)

        assertEquals(AppLanguage.ENGLISH, original.language)
        assertEquals(AppLanguage.VIETNAMESE, modified.language)
    }

    @Test
    fun `app language enum values`() {
        val languages = AppLanguage.values()
        assertTrue(languages.contains(AppLanguage.ENGLISH))
        assertTrue(languages.contains(AppLanguage.VIETNAMESE))
    }
}
