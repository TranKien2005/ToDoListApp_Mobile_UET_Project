package com.example.todolist.data.mapper

import com.example.todolist.data.local.entity.SettingsEntity
import com.example.todolist.core.model.Settings
import com.example.todolist.core.model.AppLanguage

object SettingsEntityMapper {
    fun toDomain(entity: SettingsEntity): Settings {
        return Settings(
            id = entity.id,
            language = try { 
                AppLanguage.valueOf(entity.language) 
            } catch (e: Exception) { 
                AppLanguage.ENGLISH 
            },
            taskReminderMinutes = entity.taskReminderMinutes,
            notifyDailyMissions = entity.notifyDailyMissions,
            notifyWeeklyMissions = entity.notifyWeeklyMissions,
            notifyMonthlyMissions = entity.notifyMonthlyMissions,
            dailySummaryHour = entity.dailySummaryHour,
            missionDeadlineWarningMinutes = entity.missionDeadlineWarningMinutes,
            overdueNotificationEnabled = entity.overdueNotificationEnabled
        )
    }

    fun fromDomain(settings: Settings): SettingsEntity {
        return SettingsEntity(
            id = settings.id,
            language = settings.language.name,
            taskReminderMinutes = settings.taskReminderMinutes,
            notifyDailyMissions = settings.notifyDailyMissions,
            notifyWeeklyMissions = settings.notifyWeeklyMissions,
            notifyMonthlyMissions = settings.notifyMonthlyMissions,
            dailySummaryHour = settings.dailySummaryHour,
            missionDeadlineWarningMinutes = settings.missionDeadlineWarningMinutes,
            overdueNotificationEnabled = settings.overdueNotificationEnabled
        )
    }
}
