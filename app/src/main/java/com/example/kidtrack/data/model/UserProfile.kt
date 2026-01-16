package com.example.kidtrack.data.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @NonNull
    val name: String,
    @NonNull
    val age: Int,
    val photoUrl: String? = null // Nullable as it's optional
)