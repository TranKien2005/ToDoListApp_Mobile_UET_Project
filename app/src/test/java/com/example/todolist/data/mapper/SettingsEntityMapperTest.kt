package com.example.todolist.data.mapper

import com.example.todolist.core.model.Settings
import com.example.todolist.core.model.AppLanguage
import com.example.todolist.data.local.entity.SettingsEntity
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for SettingsEntityMapper
 */
class SettingsEntityMapperTest {

    @Test
    fun `fromDomain converts Settings to SettingsEntity`() {
        val settings = Settings(
            id = 1,
            language = AppLanguage.VIETNAMESE,
            taskReminderMinutes = 30,
            notifyDailyMissions = false,
            notifyWeeklyMissions = true,
            notifyMonthlyMissions = false,
            dailySummaryHour = 9,
            missionDeadlineWarningMinutes = 120,
            overdueNotificationEnabled = false
        )

        val entity = SettingsEntityMapper.fromDomain(settings)

        assertEquals(1, entity.id)
        assertEquals("VIETNAMESE", entity.language)
        assertEquals(30, entity.taskReminderMinutes)
        assertFalse(entity.notifyDailyMissions)
        assertTrue(entity.notifyWeeklyMissions)
        assertFalse(entity.notifyMonthlyMissions)
        assertEquals(9, entity.dailySummaryHour)
        assertEquals(120, entity.missionDeadlineWarningMinutes)
        assertFalse(entity.overdueNotificationEnabled)
    }

    @Test
    fun `toDomain converts SettingsEntity to Settings`() {
        val entity = SettingsEntity(
            id = 1,
            language = "ENGLISH",
            taskReminderMinutes = 15,
            notifyDailyMissions = true,
            notifyWeeklyMissions = true,
            notifyMonthlyMissions = true,
            dailySummaryHour = 7,
            missionDeadlineWarningMinutes = 60,
            overdueNotificationEnabled = true
        )

        val settings = SettingsEntityMapper.toDomain(entity)

        assertEquals(1, settings.id)
        assertEquals(AppLanguage.ENGLISH, settings.language)
        assertEquals(15, settings.taskReminderMinutes)
        assertTrue(settings.notifyDailyMissions)
        assertEquals(7, settings.dailySummaryHour)
    }

    @Test
    fun `toDomain handles invalid language`() {
        val entity = SettingsEntity(
            language = "INVALID_LANG"
        )

        val settings = SettingsEntityMapper.toDomain(entity)

        assertEquals(AppLanguage.ENGLISH, settings.language)
    }

    @Test
    fun `round trip conversion preserves data`() {
        val original = Settings(
            language = AppLanguage.VIETNAMESE,
            taskReminderMinutes = 45,
            notifyDailyMissions = false,
            dailySummaryHour = 10
        )

        val entity = SettingsEntityMapper.fromDomain(original)
        val converted = SettingsEntityMapper.toDomain(entity)

        assertEquals(original.language, converted.language)
        assertEquals(original.taskReminderMinutes, converted.taskReminderMinutes)
        assertEquals(original.notifyDailyMissions, converted.notifyDailyMissions)
        assertEquals(original.dailySummaryHour, converted.dailySummaryHour)
    }

    @Test
    fun `all languages can be converted`() {
        AppLanguage.values().forEach { lang ->
            val settings = Settings(language = lang)
            val entity = SettingsEntityMapper.fromDomain(settings)
            assertEquals(lang.name, entity.language)
        }
    }
}
