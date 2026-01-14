package com.example.kidtrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class Activity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val description: String,
    val notes: String,
    val date: String,
    val time: String,
    val profileId: Long = 0 // Link to child profile
)