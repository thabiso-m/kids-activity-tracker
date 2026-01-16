package com.example.kidtrack.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.kidtrack.utils.NotificationHelper
import com.example.kidtrack.utils.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.kidtrack.data.database.KidTrackDatabase
import com.example.kidtrack.data.repository.KidTrackRepository

class ReminderReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("REMINDER_ID", -1L)
        val reminderTimeMinutes = intent.getIntExtra("REMINDER_TIME_MINUTES", -1)
        val activityId = intent.getLongExtra("ACTIVITY_ID", -1L)
        
        if (reminderId == -1L || activityId == -1L) {
            return
        }
        
        val reminderTime = if (reminderTimeMinutes >= 0) {
            DateTimeUtils.minutesToTimeString(reminderTimeMinutes)
        } else {
            ""
        }
        
        // Fetch activity details and send notification
        val database = KidTrackDatabase.getDatabase(context)
        val repository = KidTrackRepository(database)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val activity = repository.getActivityById(activityId)
                
                val title = "KidTrack Reminder"
                val message = if (activity != null) {
                    "Time for: ${activity.category} - ${activity.description}"
                } else {
                    "You have a scheduled activity at $reminderTime"
                }
                
                NotificationHelper.sendNotification(context, title, message)
            } catch (e: Exception) {
                // Fallback notification if we can't fetch activity
                NotificationHelper.sendNotification(
                    context,
                    "KidTrack Reminder",
                    "You have a scheduled activity at $reminderTime"
                )
            }
        }
    }
}
