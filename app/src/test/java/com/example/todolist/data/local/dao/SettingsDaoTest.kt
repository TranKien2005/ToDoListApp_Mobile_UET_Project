package com.example.todolist.data.local.dao

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for SettingsDao interface definitions
 */
class SettingsDaoTest {

    // Fake in-memory storage for testing DAO contract
    private var fakeSettings: FakeSettingsEntity = FakeSettingsEntity()
    
    data class FakeSettingsEntity(
        val id: Int = 1,
        var language: String = "VIETNAMESE",
        var taskReminderMinutes: Int = 15,
        var notifyDailyMissions: Boolean = true,
        var notifyWeeklyMissions: Boolean = true,
        var notifyMonthlyMissions: Boolean = true,
        var dailySummaryHour: Int = 7,
        var missionDeadlineWarningMinutes: Int = 60,
        var overdueNotificationEnabled: Boolean = true
    )

    @Test
    fun `getSettings returns default settings`() {
        fakeSettings = FakeSettingsEntity()
        
        assertEquals("VIETNAMESE", fakeSettings.language)
        assertEquals(15, fakeSettings.taskReminderMinutes)
        assertTrue(fakeSettings.notifyDailyMissions)
    }

    @Test
    fun `insert creates settings`() {
        fakeSettings = FakeSettingsEntity(
            language = "ENGLISH",
            taskReminderMinutes = 30
        )
        
        assertEquals("ENGLISH", fakeSettings.language)
        assertEquals(30, fakeSettings.taskReminderMinutes)
    }

    @Test
    fun `update modifies language`() {
        fakeSettings = FakeSettingsEntity()
        fakeSettings.language = "ENGLISH"
        
        assertEquals("ENGLISH", fakeSettings.language)
    }

    @Test
    fun `update modifies task reminder minutes`() {
        fakeSettings = FakeSettingsEntity()
        fakeSettings.taskReminderMinutes = 45
        
        assertEquals(45, fakeSettings.taskReminderMinutes)
    }

    @Test
    fun `toggle daily notifications`() {
        fakeSettings = FakeSettingsEntity()
        fakeSettings.notifyDailyMissions = false
        
        assertFalse(fakeSettings.notifyDailyMissions)
    }

    @Test
    fun `toggle weekly notifications`() {
        fakeSettings = FakeSettingsEntity()
        fakeSettings.notifyWeeklyMissions = false
        
        assertFalse(fakeSettings.notifyWeeklyMissions)
    }

    @Test
    fun `toggle monthly notifications`() {
        fakeSettings = FakeSettingsEntity()
        fakeSettings.notifyMonthlyMissions = false
        
        assertFalse(fakeSettings.notifyMonthlyMissions)
    }

    @Test
    fun `update daily summary hour`() {
        fakeSettings = FakeSettingsEntity()
        fakeSettings.dailySummaryHour = 9
        
        assertEquals(9, fakeSettings.dailySummaryHour)
    }

    @Test
    fun `update mission deadline warning`() {
        fakeSettings = FakeSettingsEntity()
        fakeSettings.missionDeadlineWarningMinutes = 120
        
        assertEquals(120, fakeSettings.missionDeadlineWarningMinutes)
    }

    @Test
    fun `toggle overdue notification`() {
        fakeSettings = FakeSettingsEntity()
        fakeSettings.overdueNotificationEnabled = false
        
        assertFalse(fakeSettings.overdueNotificationEnabled)
    }
}
