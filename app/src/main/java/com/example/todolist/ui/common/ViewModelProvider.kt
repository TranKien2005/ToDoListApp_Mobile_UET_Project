package com.example.todolist.ui.common

import android.content.Context
import com.example.todolist.di.AppModule
import com.example.todolist.feature.home.HomeViewModel
import com.example.todolist.feature.mission.MissionViewModel

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

}