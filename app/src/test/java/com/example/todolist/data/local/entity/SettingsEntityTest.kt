package com.example.todolist.data.local.entity

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for SettingsEntity
 */
class SettingsEntityTest {

    @Test
    fun `create settings entity with all fields`() {
        val entity = SettingsEntity(
            id = 1,
            language = "ENGLISH",
            taskReminderMinutes = 30,
            notifyDailyMissions = false,
            notifyWeeklyMissions = true,
            notifyMonthlyMissions = false,
            dailySummaryHour = 9,
            missionDeadlineWarningMinutes = 120,
            overdueNotificationEnabled = true
        )

        assertEquals(1, entity.id)
        assertEquals("ENGLISH", entity.language)
        assertEquals(30, entity.taskReminderMinutes)
        assertFalse(entity.notifyDailyMissions)
        assertTrue(entity.notifyWeeklyMissions)
        assertEquals(9, entity.dailySummaryHour)
        assertEquals(120, entity.missionDeadlineWarningMinutes)
    }

    @Test
    fun `create settings entity with default values`() {
        val entity = SettingsEntity()

        assertEquals(1, entity.id)
        assertEquals("VIETNAMESE", entity.language)
        assertEquals(15, entity.taskReminderMinutes)
        assertTrue(entity.notifyDailyMissions)
        assertTrue(entity.notifyWeeklyMissions)
        assertTrue(entity.notifyMonthlyMissions)
        assertEquals(7, entity.dailySummaryHour)
        assertEquals(60, entity.missionDeadlineWarningMinutes)
        assertTrue(entity.overdueNotificationEnabled)
    }

    @Test
    fun `settings entity copy works`() {
        val original = SettingsEntity()
        val modified = original.copy(language = "ENGLISH")

        assertEquals("VIETNAMESE", original.language)
        assertEquals("ENGLISH", modified.language)
    }
}
