package com.example.kidtrack.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kidtrack.data.model.Reminder

@Dao
interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): Reminder?

    @Query("SELECT * FROM reminders ORDER BY timeMinutes ASC")
    suspend fun getAllReminders(): List<Reminder>
    
    @Query("SELECT * FROM reminders WHERE profileId = :profileId ORDER BY timeMinutes ASC")
    suspend fun getRemindersByProfile(profileId: Long): List<Reminder>
    
    @Query("SELECT * FROM reminders WHERE associatedActivityId = :activityId")
    suspend fun getRemindersByActivity(activityId: Long): List<Reminder>

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)
    
    @Query("DELETE FROM reminders WHERE profileId = :profileId")
    suspend fun deleteRemindersByProfile(profileId: Long)
    
    @Query("DELETE FROM reminders WHERE associatedActivityId = :activityId")
    suspend fun deleteRemindersByActivity(activityId: Long)
}