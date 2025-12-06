package com.example.todolist.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.todolist.data.local.dao.MissionDao
import com.example.todolist.data.local.dao.TaskDao
import com.example.todolist.data.local.dao.UserDao
import com.example.todolist.data.local.dao.SettingsDao
import com.example.todolist.data.local.dao.NotificationDao
import com.example.todolist.data.local.entity.MissionEntity
import com.example.todolist.data.local.entity.TaskEntity
import com.example.todolist.data.local.entity.UserEntity
import com.example.todolist.data.local.entity.SettingsEntity
import com.example.todolist.data.local.entity.NotificationEntity

@Database(
    entities = [TaskEntity::class, MissionEntity::class, UserEntity::class, SettingsEntity::class, NotificationEntity::class],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun missionDao(): MissionDao
    abstract fun userDao(): UserDao
    abstract fun settingsDao(): SettingsDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "todolist_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
