package com.example.kidtrack.data.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @NonNull
    val timeMinutes: Int, // Time as minutes since midnight (0-1439)
    @NonNull
    val frequency: String, // "once", "daily", "weekly", "monthly"
    @NonNull
    val associatedActivityId: Long,
    @NonNull
    val profileId: Long = 0 // Link to child profile
)