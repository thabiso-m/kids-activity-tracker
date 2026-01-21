package com.example.kidtrack.data.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @NonNull
    val name: String = "", // Reminder name/title
    @NonNull
    val timeMinutes: Int, // Time as minutes since midnight (0-1439)
    @NonNull
    val frequency: String, // "once", "daily", "weekly", "monthly"
    @NonNull
    val associatedActivityId: Long,
    @NonNull
    val profileId: Long = 0, // Link to child profile
    @NonNull
    val daysBefore: Int = 0, // Number of days before the activity date to trigger reminder (0 = same day)
    @NonNull
    val eventDateTimestamp: Long = 0, // The actual event date (from associated activity)
    @NonNull
    val snoozeEnabled: Boolean = true // Whether snooze option is enabled for this reminder
)