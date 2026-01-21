package com.example.kidtrack.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.example.kidtrack.data.model.Reminder
import com.example.kidtrack.receivers.ReminderReceiver
import java.util.Calendar

object ReminderScheduler {
    
    /**
     * Schedule a reminder notification
     */
    fun scheduleReminder(context: Context, reminder: Reminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Check for exact alarm permission on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(
                    context,
                    "Please allow exact alarm scheduling in settings",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        }
        
        val timeString = DateTimeUtils.minutesToTimeString(reminder.timeMinutes)
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("REMINDER_ID", reminder.id)
            putExtra("REMINDER_TIME_MINUTES", reminder.timeMinutes)
            putExtra("ACTIVITY_ID", reminder.associatedActivityId)
            putExtra("DAYS_BEFORE", reminder.daysBefore)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val calendar = getNextReminderTime(reminder.timeMinutes, reminder.frequency, reminder.eventDateTimestamp, reminder.daysBefore)
        
        // Schedule the alarm
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            
            val daysBeforeText = if (reminder.daysBefore > 0) {
                " (${reminder.daysBefore} day${if (reminder.daysBefore > 1) "s" else ""} before event)"
            } else ""
            
            Toast.makeText(
                context,
                "Reminder scheduled for $timeString$daysBeforeText",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Failed to schedule reminder: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    /**
     * Cancel a scheduled reminder
     */
    fun cancelReminder(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
    
    /**
     * Calculate the next reminder time based on current time and frequency
     */
    private fun getNextReminderTime(timeMinutes: Int, frequency: String, eventDateTimestamp: Long = 0, daysBefore: Int = 0): Calendar {
        val calendar = Calendar.getInstance()
        
        // If we have an event date and daysBefore is set, calculate reminder date based on event
        if (eventDateTimestamp > 0 && daysBefore > 0) {
            calendar.timeInMillis = eventDateTimestamp
            // Subtract the days before
            calendar.add(Calendar.DAY_OF_MONTH, -daysBefore)
        }
        
        // Convert minutes to hours and minutes
        val hour = timeMinutes / 60
        val minute = timeMinutes % 60
        
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        // If the time has already passed, schedule for next occurrence
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            // For event-based reminders (with daysBefore), don't reschedule if time has passed
            if (eventDateTimestamp > 0 && daysBefore > 0) {
                // If reminder time has passed, the event likely already happened
                // Just schedule for 1 minute from now as a fallback
                calendar.timeInMillis = System.currentTimeMillis() + 60000
            } else {
                // For recurring reminders, schedule next occurrence
                when (frequency.lowercase()) {
                    "daily" -> calendar.add(Calendar.DAY_OF_MONTH, 1)
                    "weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                    "monthly" -> calendar.add(Calendar.MONTH, 1)
                    else -> calendar.add(Calendar.DAY_OF_MONTH, 1) // Default to daily
                }
            }
        }
        
        return calendar
    }
    
    /**
     * Check if the app can schedule exact alarms (Android 12+)
     */
    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
    
    /**
     * Open settings to allow exact alarm scheduling
     */
    fun openAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Unable to open alarm settings",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
