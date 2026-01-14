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

    @Query("SELECT * FROM activities ORDER BY date ASC")
    suspend fun getAllActivities(): List<Activity>

    @Query("DELETE FROM activities WHERE id = :activityId")
    suspend fun deleteActivity(activityId: Long)
}