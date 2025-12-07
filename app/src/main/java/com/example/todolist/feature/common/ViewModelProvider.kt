package com.example.todolist.feature.common

import android.content.Context
import com.example.todolist.di.AppModule
import com.example.todolist.feature.analysis.MissionAnalysisViewModel
import com.example.todolist.feature.home.HomeViewModel
import com.example.todolist.feature.mission.MissionViewModel
import com.example.todolist.feature.notification.NotificationViewModel
import com.example.todolist.feature.settings.SettingsViewModel
import com.example.todolist.feature.user.UserViewModel

object ViewModelProvider {

    // Cache AppModule để tránh tạo mới nhiều lần
    private var appModule: AppModule? = null

    private fun getAppModule(context: Context): AppModule {
        return appModule ?: AppModule(context.applicationContext).also { appModule = it }
    }

    fun provideHomeViewModel(context: Context): HomeViewModel {
        val module = getAppModule(context)
        val taskUseCases = module.domainModule.taskUseCases
        return HomeViewModel(taskUseCases)
    }

    fun provideMissionViewModel(context: Context): MissionViewModel {
        val module = getAppModule(context)
        val missionUseCases = module.domainModule.missionUseCases
        val notificationUseCases = module.domainModule.notificationUseCases
        return MissionViewModel(missionUseCases, notificationUseCases)
    }

    fun provideAddItemViewModel(context: Context): AddItemViewModel {
        val module = getAppModule(context)
        val taskUseCases = module.domainModule.taskUseCases
        val missionUseCases = module.domainModule.missionUseCases
        val notificationUseCases = module.domainModule.notificationUseCases
        val settingsUseCases = module.domainModule.settingsUseCases
        return AddItemViewModel(taskUseCases, missionUseCases, notificationUseCases, settingsUseCases)
    }

    fun provideMissionAnalysisViewModel(context: Context): MissionAnalysisViewModel {
        val module = getAppModule(context)
        val missionUseCases = module.domainModule.missionUseCases
        return MissionAnalysisViewModel(missionUseCases.getMissionStats)
    }

    fun provideUserViewModel(context: Context): UserViewModel {
        val module = getAppModule(context)
        val userUseCases = module.domainModule.userUseCases
        return UserViewModel(userUseCases)
    }

    fun provideSettingsViewModel(context: Context): SettingsViewModel {
        val module = getAppModule(context)
        val settingsUseCases = module.domainModule.settingsUseCases
        return SettingsViewModel(settingsUseCases)
    }

    fun provideNotificationViewModel(context: Context): NotificationViewModel {
        val module = getAppModule(context)
        val notificationUseCases = module.domainModule.notificationUseCases
        return NotificationViewModel(notificationUseCases)
    }

    fun provideVoiceAssistantViewModel(context: Context): com.example.todolist.feature.voice.VoiceAssistantViewModel {
        val module = getAppModule(context)
        val taskUseCases = module.domainModule.taskUseCases
        val missionUseCases = module.domainModule.missionUseCases
        val aiUseCases = module.domainModule.aiUseCases
        return com.example.todolist.feature.voice.VoiceAssistantViewModel(taskUseCases, missionUseCases, aiUseCases, context)
    }
}