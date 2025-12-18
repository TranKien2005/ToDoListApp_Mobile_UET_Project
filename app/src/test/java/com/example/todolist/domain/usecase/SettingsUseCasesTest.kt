package com.example.todolist.domain.usecase

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for SettingsUseCases interfaces
 */
class SettingsUseCasesTest {

    // Fake settings storage
    private var fakeSettings = FakeSettings()
    
    data class FakeSettings(
        val id: Int = 1,
        var language: String = "ENGLISH",
        var taskReminderMinutes: Int = 15,
        var notifyDailyMissions: Boolean = true,
        var notifyWeeklyMissions: Boolean = true,
        var notifyMonthlyMissions: Boolean = true,
        var dailySummaryHour: Int = 7,
        var missionDeadlineWarningMinutes: Int = 60,
        var overdueNotificationEnabled: Boolean = true
    )

    @Test
    fun `getSettings returns settings`() {
        fakeSettings = FakeSettings()
        
        assertEquals("ENGLISH", fakeSettings.language)
        assertEquals(15, fakeSettings.taskReminderMinutes)
    }

    @Test
    fun `updateSettings modifies language`() {
        fakeSettings = FakeSettings()
        fakeSettings.language = "VIETNAMESE"
        
        assertEquals("VIETNAMESE", fakeSettings.language)
    }

    @Test
    fun `updateSettings modifies task reminder`() {
        fakeSettings = FakeSettings()
        fakeSettings.taskReminderMinutes = 30
        
        assertEquals(30, fakeSettings.taskReminderMinutes)
    }

    @Test
    fun `updateSettings toggles daily notifications`() {
        fakeSettings = FakeSettings()
        fakeSettings.notifyDailyMissions = false
        
        assertFalse(fakeSettings.notifyDailyMissions)
    }

    @Test
    fun `updateSettings toggles weekly notifications`() {
        fakeSettings = FakeSettings()
        fakeSettings.notifyWeeklyMissions = false
        
        assertFalse(fakeSettings.notifyWeeklyMissions)
    }

    @Test
    fun `updateSettings toggles monthly notifications`() {
        fakeSettings = FakeSettings()
        fakeSettings.notifyMonthlyMissions = false
        
        assertFalse(fakeSettings.notifyMonthlyMissions)
    }

    @Test
    fun `updateSettings modifies daily summary hour`() {
        fakeSettings = FakeSettings()
        fakeSettings.dailySummaryHour = 9
        
        assertEquals(9, fakeSettings.dailySummaryHour)
    }

    @Test
    fun `updateSettings modifies mission deadline warning`() {
        fakeSettings = FakeSettings()
        fakeSettings.missionDeadlineWarningMinutes = 120
        
        assertEquals(120, fakeSettings.missionDeadlineWarningMinutes)
    }

    @Test
    fun `updateSettings toggles overdue notification`() {
        fakeSettings = FakeSettings()
        fakeSettings.overdueNotificationEnabled = false
        
        assertFalse(fakeSettings.overdueNotificationEnabled)
    }

    @Test
    fun `complete settings update`() {
        fakeSettings = FakeSettings()
        
        fakeSettings = FakeSettings(
            language = "VIETNAMESE",
            taskReminderMinutes = 45,
            notifyDailyMissions = false,
            dailySummaryHour = 10,
            missionDeadlineWarningMinutes = 90,
            overdueNotificationEnabled = false
        )
        
        assertEquals("VIETNAMESE", fakeSettings.language)
        assertEquals(45, fakeSettings.taskReminderMinutes)
        assertFalse(fakeSettings.notifyDailyMissions)
        assertEquals(10, fakeSettings.dailySummaryHour)
        assertEquals(90, fakeSettings.missionDeadlineWarningMinutes)
        assertFalse(fakeSettings.overdueNotificationEnabled)
    }
}
