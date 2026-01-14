package com.example.kidtrack.data.repository

import com.example.kidtrack.data.database.KidTrackDatabase
import com.example.kidtrack.data.model.Activity
import com.example.kidtrack.data.model.Reminder
import com.example.kidtrack.data.model.ReportStatistics
import com.example.kidtrack.data.model.UserProfile
import java.text.SimpleDateFormat
import java.util.*

class KidTrackRepository(private val database: KidTrackDatabase) {

    suspend fun insertActivity(activity: Activity) {
        database.activityDao().insertActivity(activity)
    }

    suspend fun getAllActivities(): List<Activity> {
        return database.activityDao().getAllActivities()
    }

    suspend fun getActivityById(id: Long): Activity? {
        return database.activityDao().getAllActivities().find { it.id == id }
    }

    suspend fun deleteActivity(activity: Activity) {
        database.activityDao().deleteActivity(activity.id)
    }

    suspend fun getUpcomingActivities(): List<Activity> {
        return database.activityDao().getAllActivities()
    }

    suspend fun getOverdueTasks(): List<Activity> {
        return database.activityDao().getAllActivities()
    }

    suspend fun insertReminder(reminder: Reminder) {
        database.reminderDao().insertReminder(reminder)
    }

    suspend fun getAllReminders(): List<Reminder> {
        return database.reminderDao().getAllReminders()
    }

    suspend fun deleteReminder(reminder: Reminder) {
        database.reminderDao().deleteReminderById(reminder.id)
    }

    suspend fun insertUserProfile(profile: UserProfile) {
        database.profileDao().insertProfile(profile)
    }

    suspend fun getAllUserProfiles(): List<UserProfile> {
        return database.profileDao().getAllProfiles()
    }

    suspend fun deleteUserProfile(profile: UserProfile) {
        database.profileDao().deleteProfileById(profile.id)
    }

    suspend fun getWeeklySummary(): Map<String, Any> {
        return emptyMap()
    }

    suspend fun getReportStatistics(): ReportStatistics {
        val allActivities = getAllActivities()
        val calendar = Calendar.getInstance()
        val currentWeekStart = calendar.apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.time

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentWeekStartStr = dateFormat.format(currentWeekStart)

        val thisWeekActivities = allActivities.filter { activity ->
            try {
                val activityDate = dateFormat.parse(activity.date)
                activityDate?.after(currentWeekStart) == true || activityDate?.equals(currentWeekStart) == true
            } catch (e: Exception) {
                false
            }
        }

        val categoryBreakdown = allActivities.groupBy { it.category }
            .mapValues { it.value.size }

        val completionRate = if (allActivities.isNotEmpty()) {
            (allActivities.size * 75 / 100) // Mock completion rate
        } else 0

        return ReportStatistics(
            totalActivities = allActivities.size,
            completedActivities = (allActivities.size * 0.75).toInt(),
            thisWeekActivities = thisWeekActivities.size,
            completionRate = if (allActivities.isNotEmpty()) 75 else 0,
            categoryBreakdown = categoryBreakdown,
            recentActivities = allActivities.takeLast(5).reversed()
        )
    }
}