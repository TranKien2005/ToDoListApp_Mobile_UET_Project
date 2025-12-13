package com.example.todolist.data.mapper

import com.example.todolist.core.model.Settings
import com.example.todolist.data.local.entity.SettingsEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsEntityMapperTest {

    @Test
    fun `toDomain maps entity to settings correctly`() {
        val entity = SettingsEntity(
            id = 1,
            taskReminderMinutes = 30,
            notifyDailyMissions = true,
            notifyWeeklyMissions = false,
            notifyMonthlyMissions = true,
            dailySummaryHour = 8,
            missionDeadlineWarningMinutes = 120,
            overdueNotificationEnabled = false
        )
        
        val settings = SettingsEntityMapper.toDomain(entity)
        
        assertEquals(1, settings.id)
        assertEquals(30, settings.taskReminderMinutes)
        assertEquals(true, settings.notifyDailyMissions)
        assertEquals(false, settings.notifyWeeklyMissions)
        assertEquals(true, settings.notifyMonthlyMissions)
        assertEquals(8, settings.dailySummaryHour)
        assertEquals(120, settings.missionDeadlineWarningMinutes)
        assertEquals(false, settings.overdueNotificationEnabled)
    }

    @Test
    fun `fromDomain maps settings to entity correctly`() {
        val settings = Settings(
            id = 1,
            taskReminderMinutes = 30,
            notifyDailyMissions = true,
            notifyWeeklyMissions = false,
            notifyMonthlyMissions = true,
            dailySummaryHour = 8,
            missionDeadlineWarningMinutes = 120,
            overdueNotificationEnabled = false
        )
        
        val entity = SettingsEntityMapper.fromDomain(settings)
        
        assertEquals(1, entity.id)
        assertEquals(30, entity.taskReminderMinutes)
        assertEquals(true, entity.notifyDailyMissions)
        assertEquals(false, entity.notifyWeeklyMissions)
        assertEquals(true, entity.notifyMonthlyMissions)
        assertEquals(8, entity.dailySummaryHour)
        assertEquals(120, entity.missionDeadlineWarningMinutes)
        assertEquals(false, entity.overdueNotificationEnabled)
    }

    @Test
    fun `roundtrip mapping preserves all fields`() {
        val original = Settings(
            id = 1,
            taskReminderMinutes = 45,
            notifyDailyMissions = false,
            notifyWeeklyMissions = true,
            notifyMonthlyMissions = false,
            dailySummaryHour = 9,
            missionDeadlineWarningMinutes = 30,
            overdueNotificationEnabled = true
        )
        
        val entity = SettingsEntityMapper.fromDomain(original)
        val mapped = SettingsEntityMapper.toDomain(entity)
        
        assertEquals(original.id, mapped.id)
        assertEquals(original.taskReminderMinutes, mapped.taskReminderMinutes)
        assertEquals(original.notifyDailyMissions, mapped.notifyDailyMissions)
        assertEquals(original.notifyWeeklyMissions, mapped.notifyWeeklyMissions)
        assertEquals(original.notifyMonthlyMissions, mapped.notifyMonthlyMissions)
        assertEquals(original.dailySummaryHour, mapped.dailySummaryHour)
        assertEquals(original.missionDeadlineWarningMinutes, mapped.missionDeadlineWarningMinutes)
        assertEquals(original.overdueNotificationEnabled, mapped.overdueNotificationEnabled)
    }

    @Test
    fun `default values are correctly mapped`() {
        val defaultSettings = Settings()
        
        val entity = SettingsEntityMapper.fromDomain(defaultSettings)
        val mapped = SettingsEntityMapper.toDomain(entity)
        
        assertEquals(1, mapped.id)
        assertEquals(15, mapped.taskReminderMinutes)
        assertEquals(true, mapped.notifyDailyMissions)
        assertEquals(true, mapped.notifyWeeklyMissions)
        assertEquals(true, mapped.notifyMonthlyMissions)
        assertEquals(7, mapped.dailySummaryHour)
        assertEquals(60, mapped.missionDeadlineWarningMinutes)
        assertEquals(true, mapped.overdueNotificationEnabled)
    }

    @Test
    fun `edge case daily summary hour boundaries`() {
        // Test hour 0 (midnight)
        val settingsHour0 = Settings(dailySummaryHour = 0)
        val mappedHour0 = SettingsEntityMapper.toDomain(
            SettingsEntityMapper.fromDomain(settingsHour0)
        )
        assertEquals(0, mappedHour0.dailySummaryHour)
        
        // Test hour 23 (11 PM)
        val settingsHour23 = Settings(dailySummaryHour = 23)
        val mappedHour23 = SettingsEntityMapper.toDomain(
            SettingsEntityMapper.fromDomain(settingsHour23)
        )
        assertEquals(23, mappedHour23.dailySummaryHour)
    }

    @Test
    fun `all boolean combinations are mapped correctly`() {
        val booleanCombinations = listOf(
            Triple(true, true, true),
            Triple(true, true, false),
            Triple(true, false, true),
            Triple(true, false, false),
            Triple(false, true, true),
            Triple(false, true, false),
            Triple(false, false, true),
            Triple(false, false, false)
        )
        
        booleanCombinations.forEach { (daily, weekly, monthly) ->
            val settings = Settings(
                notifyDailyMissions = daily,
                notifyWeeklyMissions = weekly,
                notifyMonthlyMissions = monthly
            )
            
            val mapped = SettingsEntityMapper.toDomain(
                SettingsEntityMapper.fromDomain(settings)
            )
            
            assertEquals(daily, mapped.notifyDailyMissions)
            assertEquals(weekly, mapped.notifyWeeklyMissions)
            assertEquals(monthly, mapped.notifyMonthlyMissions)
        }
    }
}
