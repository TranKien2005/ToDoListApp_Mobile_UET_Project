package com.example.todolist.data.local.di

import android.content.Context
import com.example.todolist.data.local.database.AppDatabase
import com.example.todolist.data.local.dao.MissionDao
import com.example.todolist.data.local.dao.TaskDao

object LocalModule {
    /**
     * Provide the Room AppDatabase instance. Uses the existing AppDatabase.getInstance(context).
     */
    fun provideAppDatabase(context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    /**
     * Provide DAOs from a database instance.
     */
    fun provideMissionDao(db: AppDatabase): MissionDao = db.missionDao()

    fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()

    /**
     * Convenience overloads that accept a Context and obtain the DB internally.
     */
    fun provideMissionDao(context: Context): MissionDao = provideMissionDao(provideAppDatabase(context))

    fun provideTaskDao(context: Context): TaskDao = provideTaskDao(provideAppDatabase(context))
}

