package com.example.kidtrack.data.repository

import android.util.Log
import com.example.kidtrack.data.database.KidTrackDatabase
import com.example.kidtrack.data.model.Activity
import com.example.kidtrack.data.model.Reminder
import com.example.kidtrack.data.model.ReportStatistics
import com.example.kidtrack.data.model.UserProfile
import com.example.kidtrack.utils.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository class for managing data operations.
 * Provides a clean API for accessing and manipulating app data.
 */
class KidTrackRepository(private val database: KidTrackDatabase) {

    companion object {
        private const val TAG = "KidTrackRepository"
    }

    // ==================== Activity Operations ====================

    /**
     * Insert or update an activity in the database
     * @throws Exception if database operation fails
     */
    suspend fun insertActivity(activity: Activity) = withContext(Dispatchers.IO) {
        try {
            database.activityDao().insertActivity(activity)
            Log.d(TAG, "Activity inserted successfully: ${activity.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting activity", e)
            throw e
        }
    }

    /**
     * Get all activities from the database
     * @return List of all activities, sorted by date and time
     * @throws Exception if database operation fails
     */
    suspend fun getAllActivities(): List<Activity> = withContext(Dispatchers.IO) {
        try {
            database.activityDao().getAllActivities()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all activities", e)
            throw e
        }
    }

    /**
     * Get a specific activity by ID
     * @param id The activity ID
     * @return Activity if found, null otherwise
     * @throws Exception if database operation fails
     */
    suspend fun getActivityById(id: Long): Activity? = withContext(Dispatchers.IO) {
        try {
            database.activityDao().getActivityById(id)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting activity by id: $id", e)
            throw e
        }
    }

    /**
     * Delete an activity from the database
     * @param activity The activity to delete
     * @throws Exception if database operation fails
     */
    suspend fun deleteActivity(activity: Activity) = withContext(Dispatchers.IO) {
        try {
            database.activityDao().deleteActivity(activity.id)
            Log.d(TAG, "Activity deleted successfully: ${activity.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting activity", e)
            throw e
        }
    }

    /**
     * Get upcoming activities (today and future)
     * @return List of activities with timestamps >= today
     * @throws Exception if database operation fails
     */
    suspend fun getUpcomingActivities(): List<Activity> = withContext(Dispatchers.IO) {
        try {
            val todayStart = DateTimeUtils.getCurrentTimestamp()
            database.activityDao().getAllActivities().filter { activity ->
                activity.dateTimestamp >= todayStart || DateTimeUtils.isToday(activity.dateTimestamp)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting upcoming activities", e)
            throw e
        }
    }

    /**
     * Get overdue tasks (activities from the past that weren't completed)
     * @return List of activities with timestamps < today
     * @throws Exception if database operation fails
     */
    suspend fun getOverdueTasks(): List<Activity> = withContext(Dispatchers.IO) {
        try {
            database.activityDao().getAllActivities().filter { activity ->
                DateTimeUtils.isPast(activity.dateTimestamp)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting overdue tasks", e)
            throw e
        }
    }

    /**
     * Get activities for a specific profile
     * @param profileId The profile ID
     * @return List of activities for the profile
     * @throws Exception if database operation fails
     */
    suspend fun getActivitiesByProfile(profileId: Long): List<Activity> = withContext(Dispatchers.IO) {
        try {
            database.activityDao().getActivitiesByProfile(profileId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting activities by profile: $profileId", e)
            throw e
        }
    }

    // ==================== Reminder Operations ====================

    /**
     * Insert or update a reminder in the database
     * @throws Exception if database operation fails
     */
    suspend fun insertReminder(reminder: Reminder) = withContext(Dispatchers.IO) {
        try {
            database.reminderDao().insertReminder(reminder)
            Log.d(TAG, "Reminder inserted successfully: ${reminder.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting reminder", e)
            throw e
        }
    }

    /**
     * Get all reminders from the database
     * @return List of all reminders, sorted by time
     * @throws Exception if database operation fails
     */
    suspend fun getAllReminders(): List<Reminder> = withContext(Dispatchers.IO) {
        try {
            database.reminderDao().getAllReminders()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all reminders", e)
            throw e
        }
    }
    
    /**
     * Get a specific reminder by ID
     * @param id The reminder ID
     * @return Reminder if found, null otherwise
     * @throws Exception if database operation fails
     */
    suspend fun getReminderById(id: Long): Reminder? = withContext(Dispatchers.IO) {
        try {
            database.reminderDao().getReminderById(id)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting reminder by id: $id", e)
            throw e
        }
    }

    /**
     * Delete a reminder from the database
     * @param reminder The reminder to delete
     * @throws Exception if database operation fails
     */
    suspend fun deleteReminder(reminder: Reminder) = withContext(Dispatchers.IO) {
        try {
            database.reminderDao().deleteReminderById(reminder.id)
            Log.d(TAG, "Reminder deleted successfully: ${reminder.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting reminder", e)
            throw e
        }
    }

    // ==================== Profile Operations ====================

    /**
     * Insert or update a user profile in the database
     * @throws Exception if database operation fails
     */
    suspend fun insertUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        try {
            database.profileDao().insertProfile(profile)
            Log.d(TAG, "Profile inserted successfully: ${profile.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting profile", e)
            throw e
        }
    }

    /**
     * Get all user profiles from the database
     * @return List of all profiles
     * @throws Exception if database operation fails
     */
    suspend fun getAllUserProfiles(): List<UserProfile> = withContext(Dispatchers.IO) {
        try {
            database.profileDao().getAllProfiles()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all profiles", e)
            throw e
        }
    }

    /**
     * Get a specific profile by ID
     * @param id The profile ID
     * @return UserProfile if found, null otherwise
     * @throws Exception if database operation fails
     */
    suspend fun getUserProfileById(id: Long): UserProfile? = withContext(Dispatchers.IO) {
        try {
            database.profileDao().getProfileById(id)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting profile by id: $id", e)
            throw e
        }
    }

    /**
     * Delete a user profile and all associated data
     * @param profile The profile to delete
     * @throws Exception if database operation fails
     */
    suspend fun deleteUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        try {
            // Delete all activities for this profile
            database.activityDao().deleteActivitiesByProfile(profile.id)
            // Delete all reminders for this profile
            database.reminderDao().deleteRemindersByProfile(profile.id)
            // Delete the profile
            database.profileDao().deleteProfileById(profile.id)
            Log.d(TAG, "Profile and associated data deleted successfully: ${profile.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting profile", e)
            throw e
        }
    }

    // ==================== Report Operations ====================

    /**
     * Get weekly summary of activities
     * @return Map containing weekly statistics
     * @throws Exception if database operation fails
     */
    suspend fun getWeeklySummary(): Map<String, Any> = withContext(Dispatchers.IO) {
        try {
            val startOfWeek = DateTimeUtils.getStartOfWeekTimestamp()
            val endOfWeek = DateTimeUtils.getEndOfWeekTimestamp()
            
            val weekActivities = database.activityDao().getActivitiesByDateRange(startOfWeek, endOfWeek)
            
            mapOf(
                "totalActivities" to weekActivities.size,
                "activitiesByCategory" to weekActivities.groupBy { it.category }.mapValues { it.value.size },
                "activitiesByDay" to weekActivities.groupBy { 
                    DateTimeUtils.timestampToDateString(it.dateTimestamp)
                }.mapValues { it.value.size }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting weekly summary", e)
            throw e
        }
    }

    /**
     * Get comprehensive report statistics
     * @return ReportStatistics object with calculated metrics
     * @throws Exception if database operation fails
     */
    suspend fun getReportStatistics(): ReportStatistics = withContext(Dispatchers.IO) {
        try {
            val allActivities = getAllActivities()
            val startOfWeek = DateTimeUtils.getStartOfWeekTimestamp()
            val endOfWeek = DateTimeUtils.getEndOfWeekTimestamp()

            val thisWeekActivities = allActivities.filter { activity ->
                activity.dateTimestamp in startOfWeek..endOfWeek
            }

            val completedActivities = allActivities.filter { activity ->
                DateTimeUtils.isPast(activity.dateTimestamp)
            }

            val categoryBreakdown = allActivities.groupBy { it.category }
                .mapValues { it.value.size }

            val completionRate = if (allActivities.isNotEmpty()) {
                (completedActivities.size * 100) / allActivities.size
            } else {
                0
            }

            ReportStatistics(
                totalActivities = allActivities.size,
                completedActivities = completedActivities.size,
                thisWeekActivities = thisWeekActivities.size,
                completionRate = completionRate,
                categoryBreakdown = categoryBreakdown,
                recentActivities = allActivities.takeLast(5).reversed()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting report statistics", e)
            throw e
        }
    }
}