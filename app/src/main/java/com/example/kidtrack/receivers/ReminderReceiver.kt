package com.example.kidtrack.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.kidtrack.utils.NotificationHelper
import com.example.kidtrack.utils.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.kidtrack.data.database.KidTrackDatabase
import com.example.kidtrack.data.repository.KidTrackRepository
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "ACTION_SNOOZE" -> {
                handleSnooze(context, intent)
            }
            else -> {
                handleReminder(context, intent)
            }
        }
    }
    
    private fun handleReminder(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("REMINDER_ID", -1L)
        val reminderTimeMinutes = intent.getIntExtra("REMINDER_TIME_MINUTES", -1)
        val activityId = intent.getLongExtra("ACTIVITY_ID", -1L)
        val daysBefore = intent.getIntExtra("DAYS_BEFORE", 0)
        
        if (reminderId == -1L || activityId == -1L) {
            return
        }
        
        val reminderTime = if (reminderTimeMinutes >= 0) {
            DateTimeUtils.minutesToTimeString(reminderTimeMinutes)
        } else {
            ""
        }
        
        // Fetch activity and reminder details
        val database = KidTrackDatabase.getDatabase(context)
        val repository = KidTrackRepository(database)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val activity = repository.getActivityById(activityId)
                val reminder = repository.getReminderById(reminderId)
                
                // Build title with reminder name
                val title = if (reminder != null && reminder.name.isNotBlank()) {
                    "${reminder.name}"
                } else {
                    "KidTrack Reminder"
                }
                
                // Build message with activity details
                val message = if (activity != null) {
                    val dateStr = DateTimeUtils.timestampToDateString(activity.dateTimestamp)
                    val timeStr = DateTimeUtils.minutesToTimeString(activity.timeMinutes)
                    val daysBeforeText = if (daysBefore > 0) " (Tomorrow)" else ""
                    "Activity: ${activity.category} - ${activity.description}$daysBeforeText\nScheduled: $dateStr at $timeStr"
                } else {
                    "You have a scheduled activity at $reminderTime"
                }
                
                val snoozeEnabled = reminder?.snoozeEnabled ?: true
                NotificationHelper.sendNotification(context, title, message, reminderId, activityId, snoozeEnabled)
            } catch (e: Exception) {
                // Fallback notification if we can't fetch activity
                NotificationHelper.sendNotification(
                    context,
                    "KidTrack Reminder",
                    "You have a scheduled activity at $reminderTime",
                    reminderId,
                    activityId,
                    true
                )
            }
        }
    }
    
    private fun handleSnooze(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("REMINDER_ID", -1L)
        val activityId = intent.getLongExtra("ACTIVITY_ID", -1L)
        
        if (reminderId == -1L) {
            return
        }
        
        // Schedule reminder to trigger again in 10 minutes
        val snoozeIntent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("REMINDER_ID", reminderId)
            putExtra("ACTIVITY_ID", activityId)
            putExtra("REMINDER_TIME_MINUTES", -1)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt() + 20000, // Different request code for snooze
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, 10) // Snooze for 10 minutes
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Toast.makeText(context, "Reminder snoozed for 10 minutes", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to snooze reminder", Toast.LENGTH_SHORT).show()
        }
    }
}
