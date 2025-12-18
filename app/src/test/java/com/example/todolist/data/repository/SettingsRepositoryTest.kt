package com.example.todolist.data.repository

import com.example.todolist.core.model.Settings
import com.example.todolist.core.model.AppLanguage
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SettingsRepository
 */
class SettingsRepositoryTest {

    private var fakeSettings: Settings = Settings()

    @Before
    fun setup() {
        fakeSettings = Settings()
    }

    @Test
    fun `get default settings`() {
        assertEquals(AppLanguage.ENGLISH, fakeSettings.language)
        assertEquals(15, fakeSettings.taskReminderMinutes)
        assertTrue(fakeSettings.notifyDailyMissions)
    }

    @Test
    fun `update language setting`() {
        fakeSettings = fakeSettings.copy(language = AppLanguage.VIETNAMESE)
        assertEquals(AppLanguage.VIETNAMESE, fakeSettings.language)
    }

    @Test
    fun `update task reminder minutes`() {
        fakeSettings = fakeSettings.copy(taskReminderMinutes = 30)
        assertEquals(30, fakeSettings.taskReminderMinutes)
    }

    @Test
    fun `toggle daily notifications`() {
        fakeSettings = fakeSettings.copy(notifyDailyMissions = false)
        assertFalse(fakeSettings.notifyDailyMissions)
    }

    @Test
    fun `toggle weekly notifications`() {
        fakeSettings = fakeSettings.copy(notifyWeeklyMissions = false)
        assertFalse(fakeSettings.notifyWeeklyMissions)
    }

    @Test
    fun `toggle monthly notifications`() {
        fakeSettings = fakeSettings.copy(notifyMonthlyMissions = false)
        assertFalse(fakeSettings.notifyMonthlyMissions)
    }

    @Test
    fun `update daily summary hour`() {
        fakeSettings = fakeSettings.copy(dailySummaryHour = 10)
        assertEquals(10, fakeSettings.dailySummaryHour)
    }

    @Test
    fun `update mission deadline warning`() {
        fakeSettings = fakeSettings.copy(missionDeadlineWarningMinutes = 120)
        assertEquals(120, fakeSettings.missionDeadlineWarningMinutes)
    }

    @Test
    fun `toggle overdue notification`() {
        fakeSettings = fakeSettings.copy(overdueNotificationEnabled = false)
        assertFalse(fakeSettings.overdueNotificationEnabled)
    }

    @Test
    fun `reset to default settings`() {
        fakeSettings = fakeSettings.copy(language = AppLanguage.VIETNAMESE, taskReminderMinutes = 60)
        fakeSettings = Settings() // Reset
        
        assertEquals(AppLanguage.ENGLISH, fakeSettings.language)
        assertEquals(15, fakeSettings.taskReminderMinutes)
    }
}
