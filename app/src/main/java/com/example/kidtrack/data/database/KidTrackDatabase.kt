package com.example.kidtrack.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kidtrack.data.model.Activity
import com.example.kidtrack.data.model.Reminder
import com.example.kidtrack.data.model.UserProfile
import com.example.kidtrack.data.database.dao.ActivityDao
import com.example.kidtrack.data.database.dao.ReminderDao
import com.example.kidtrack.data.database.dao.ProfileDao
import com.example.kidtrack.data.database.converters.DateConverter
import android.content.Context

@Database(entities = [Activity::class, Reminder::class, UserProfile::class], version = 5, exportSchema = false)
@TypeConverters(DateConverter::class)
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
        
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create new activities table with updated schema
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS activities_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        category TEXT NOT NULL,
                        description TEXT NOT NULL,
                        notes TEXT NOT NULL,
                        dateTimestamp INTEGER NOT NULL,
                        timeMinutes INTEGER NOT NULL,
                        profileId INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Copy data from old table, converting date and time strings to timestamp and minutes
                // Note: This migration assumes existing data uses yyyy-MM-dd and HH:mm format
                // For safety, we'll set a default timestamp (current time) and time (720 = 12:00 noon)
                database.execSQL("""
                    INSERT INTO activities_new (id, category, description, notes, dateTimestamp, timeMinutes, profileId)
                    SELECT id, category, description, notes, 
                           strftime('%s', date) * 1000, 
                           CAST(substr(time, 1, 2) AS INTEGER) * 60 + CAST(substr(time, 4, 2) AS INTEGER),
                           profileId
                    FROM activities
                """.trimIndent())
                
                // Drop old table and rename new table
                database.execSQL("DROP TABLE activities")
                database.execSQL("ALTER TABLE activities_new RENAME TO activities")
                
                // Create new reminders table with updated schema
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS reminders_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        timeMinutes INTEGER NOT NULL,
                        frequency TEXT NOT NULL,
                        associatedActivityId INTEGER NOT NULL,
                        profileId INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Copy data from old table, converting time string to minutes
                database.execSQL("""
                    INSERT INTO reminders_new (id, timeMinutes, frequency, associatedActivityId, profileId)
                    SELECT id, 
                           CAST(substr(time, 1, 2) AS INTEGER) * 60 + CAST(substr(time, 4, 2) AS INTEGER),
                           frequency, associatedActivityId, profileId
                    FROM reminders
                """.trimIndent())
                
                // Drop old table and rename new table
                database.execSQL("DROP TABLE reminders")
                database.execSQL("ALTER TABLE reminders_new RENAME TO reminders")
            }
        }
        
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add daysBefore and eventDateTimestamp columns to reminders table
                database.execSQL("ALTER TABLE reminders ADD COLUMN daysBefore INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE reminders ADD COLUMN eventDateTimestamp INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add name and snoozeEnabled columns to reminders table
                database.execSQL("ALTER TABLE reminders ADD COLUMN name TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE reminders ADD COLUMN snoozeEnabled INTEGER NOT NULL DEFAULT 1")
            }
        }

        fun getDatabase(context: Context): KidTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KidTrackDatabase::class.java,
                    "kidtrack_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .fallbackToDestructiveMigration() // Only for development - remove for production
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}