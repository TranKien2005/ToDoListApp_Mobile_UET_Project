package com.example.todolist.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 5,
    exportSchema = false
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

        // Migration from version 4 to 5: Add Google Sign-In and Calendar Sync columns
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add Google fields to users table
                database.execSQL("ALTER TABLE users ADD COLUMN googleId TEXT DEFAULT NULL")
                database.execSQL("ALTER TABLE users ADD COLUMN email TEXT DEFAULT NULL")
                database.execSQL("ALTER TABLE users ADD COLUMN isCalendarSyncEnabled INTEGER NOT NULL DEFAULT 0")
                
                // Add Google Calendar event ID to tasks table
                database.execSQL("ALTER TABLE tasks ADD COLUMN googleCalendarEventId TEXT DEFAULT NULL")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                                context.applicationContext,
                                AppDatabase::class.java,
                                "todolist_db"
                            )
                            .addMigrations(MIGRATION_4_5)
                            .fallbackToDestructiveMigration(false)
                            .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
