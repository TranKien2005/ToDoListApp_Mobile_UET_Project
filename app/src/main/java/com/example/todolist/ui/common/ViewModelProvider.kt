package com.example.todolist.ui.common

import android.content.Context
import com.example.todolist.di.AppModule
import com.example.todolist.feature.home.HomeViewModel
import com.example.todolist.feature.mission.MissionViewModel
import com.example.todolist.feature.common.AddItemViewModel
import com.example.todolist.feature.analysis.MissionAnalysisViewModel
import com.example.todolist.feature.user.UserViewModel
import com.example.todolist.feature.settings.SettingsViewModel

object ViewModelProvider {

    fun provideHomeViewModel(context: Context): HomeViewModel {
        val appModule = AppModule(context)
        val taskUseCases = appModule.domainModule.taskUseCases
        return HomeViewModel(taskUseCases)
    }

    fun provideMissionViewModel(context: Context): MissionViewModel {
        val appModule = AppModule(context)
        val missionUseCases = appModule.domainModule.missionUseCases
        return MissionViewModel(missionUseCases)
    }

    fun provideAddItemViewModel(context: Context): AddItemViewModel {
        val appModule = AppModule(context)
        val taskUseCases = appModule.domainModule.taskUseCases
        val missionUseCases = appModule.domainModule.missionUseCases
        return AddItemViewModel(taskUseCases, missionUseCases)
    }

    fun provideMissionAnalysisViewModel(context: Context): MissionAnalysisViewModel {
        val appModule = AppModule(context)
        val missionUseCases = appModule.domainModule.missionUseCases
        return MissionAnalysisViewModel(missionUseCases.getMissionStats)
    }

    fun provideUserViewModel(context: Context): UserViewModel {
        val appModule = AppModule(context)
        val userUseCases = appModule.domainModule.userUseCases
        return UserViewModel(userUseCases)
    }

    fun provideSettingsViewModel(context: Context): SettingsViewModel {
        val appModule = AppModule(context)
        val settingsUseCases = appModule.domainModule.settingsUseCases
        return SettingsViewModel(settingsUseCases)
    }
}