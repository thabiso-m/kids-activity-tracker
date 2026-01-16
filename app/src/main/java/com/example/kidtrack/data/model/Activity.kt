package com.example.kidtrack.data.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class Activity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @NonNull
    val category: String,
    @NonNull
    val description: String,
    @NonNull
    val notes: String,
    @NonNull
    val dateTimestamp: Long, // Date as timestamp (milliseconds)
    @NonNull
    val timeMinutes: Int, // Time as minutes since midnight (0-1439)
    @NonNull
    val profileId: Long = 0 // Link to child profile
)