package com.example.todolist.core.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Settings model
 */
class SettingsTest {

    @Test
    fun `default settings values`() {
        val settings = Settings()

        assertEquals(AppLanguage.ENGLISH, settings.language)
        assertEquals(15, settings.taskReminderMinutes)
        assertTrue(settings.notifyDailyMissions)
        assertTrue(settings.notifyWeeklyMissions)
        assertTrue(settings.notifyMonthlyMissions)
        assertEquals(7, settings.dailySummaryHour) // Default is 7, not 8
        assertEquals(60, settings.missionDeadlineWarningMinutes)
        assertTrue(settings.overdueNotificationEnabled)
    }

    @Test
    fun `settings with custom values`() {
        val settings = Settings(
            language = AppLanguage.VIETNAMESE,
            taskReminderMinutes = 30,
            notifyDailyMissions = false,
            dailySummaryHour = 9,
            missionDeadlineWarningMinutes = 120
        )

        assertEquals(AppLanguage.VIETNAMESE, settings.language)
        assertEquals(30, settings.taskReminderMinutes)
        assertFalse(settings.notifyDailyMissions)
        assertEquals(9, settings.dailySummaryHour)
        assertEquals(120, settings.missionDeadlineWarningMinutes)
    }

    @Test
    fun `app language enum values`() {
        val languages = AppLanguage.values()
        assertEquals(2, languages.size)
        assertEquals(AppLanguage.VIETNAMESE, AppLanguage.valueOf("VIETNAMESE"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.valueOf("ENGLISH"))
    }

    @Test
    fun `settings copy works correctly`() {
        val original = Settings()
        val modified = original.copy(language = AppLanguage.VIETNAMESE)

        assertEquals(AppLanguage.ENGLISH, original.language)
        assertEquals(AppLanguage.VIETNAMESE, modified.language)
    }

    @Test
    fun `settings equality`() {
        val settings1 = Settings()
        val settings2 = Settings()
        
        assertEquals(settings1, settings2)
    }

    @Test
    fun `settings inequality when different`() {
        val settings1 = Settings()
        val settings2 = Settings(language = AppLanguage.VIETNAMESE)
        
        assertNotEquals(settings1, settings2)
    }
}
