package com.example.kidtrack.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kidtrack.data.model.Activity

@Dao
interface ActivityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: Activity)

    @Query("SELECT * FROM activities WHERE id = :activityId")
    suspend fun getActivityById(activityId: Long): Activity?

    @Query("SELECT * FROM activities ORDER BY dateTimestamp ASC, timeMinutes ASC")
    suspend fun getAllActivities(): List<Activity>
    
    @Query("SELECT * FROM activities WHERE profileId = :profileId ORDER BY dateTimestamp ASC, timeMinutes ASC")
    suspend fun getActivitiesByProfile(profileId: Long): List<Activity>
    
    @Query("SELECT * FROM activities WHERE dateTimestamp >= :startTimestamp AND dateTimestamp <= :endTimestamp ORDER BY dateTimestamp ASC, timeMinutes ASC")
    suspend fun getActivitiesByDateRange(startTimestamp: Long, endTimestamp: Long): List<Activity>

    @Query("DELETE FROM activities WHERE id = :activityId")
    suspend fun deleteActivity(activityId: Long)
    
    @Query("DELETE FROM activities WHERE profileId = :profileId")
    suspend fun deleteActivitiesByProfile(profileId: Long)
}