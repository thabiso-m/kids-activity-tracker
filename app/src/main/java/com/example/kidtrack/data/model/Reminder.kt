package com.example.kidtrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val time: String,
    val frequency: String,
    val associatedActivityId: Long,
    val profileId: Long = 0 // Link to child profile
)