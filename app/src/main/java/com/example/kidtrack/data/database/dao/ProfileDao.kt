package com.example.kidtrack.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kidtrack.data.model.UserProfile

@Dao
interface ProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getProfileById(id: Long): UserProfile?

    @Query("SELECT * FROM profiles")
    suspend fun getAllProfiles(): List<UserProfile>

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteProfileById(id: Long)
}