package com.example.kidtrack.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kidtrack.data.model.Activity
import com.example.kidtrack.data.model.Reminder
import com.example.kidtrack.data.model.UserProfile
import com.example.kidtrack.data.database.dao.ActivityDao
import com.example.kidtrack.data.database.dao.ReminderDao
import com.example.kidtrack.data.database.dao.ProfileDao
import android.content.Context

@Database(entities = [Activity::class, Reminder::class, UserProfile::class], version = 2, exportSchema = false)
abstract class KidTrackDatabase : RoomDatabase() {

    abstract fun activityDao(): ActivityDao
    abstract fun reminderDao(): ReminderDao
    abstract fun profileDao(): ProfileDao

    companion object {
        @Volatile
        private var INSTANCE: KidTrackDatabase? = null
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add profileId column to activities table
                database.execSQL("ALTER TABLE activities ADD COLUMN profileId INTEGER NOT NULL DEFAULT 0")
                // Add profileId column to reminders table
                database.execSQL("ALTER TABLE reminders ADD COLUMN profileId INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): KidTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KidTrackDatabase::class.java,
                    "kidtrack_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}