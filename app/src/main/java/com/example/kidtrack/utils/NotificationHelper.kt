package com.example.kidtrack.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.kidtrack.MainActivity
import com.example.kidtrack.R
import com.example.kidtrack.receivers.ReminderReceiver

object NotificationHelper {

    private const val CHANNEL_ID = "kidtrack_channel"
    private const val CHANNEL_NAME = "KidTrack Notifications"
    private const val CHANNEL_DESCRIPTION = "Notifications for KidTrack activities and reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
                
                // Set custom sound
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                setSound(soundUri, audioAttributes)
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendNotification(context: Context, title: String, message: String, reminderId: Long = 0, activityId: Long = 0, snoozeEnabled: Boolean = true) {
        // Check for notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission not granted, cannot send notification
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Create snooze action
        val snoozeIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = "ACTION_SNOOZE"
            putExtra("REMINDER_ID", reminderId)
            putExtra("ACTIVITY_ID", activityId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt() + 10000, // Unique request code
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Get default notification sound
        val soundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        builder.setSmallIcon(android.R.drawable.ic_dialog_info)
        builder.setContentTitle(title)
        builder.setContentText(message)
        builder.setStyle(NotificationCompat.BigTextStyle().bigText(message))
        builder.setPriority(NotificationCompat.PRIORITY_HIGH)
        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(true)
        builder.setSound(soundUri)
        builder.setVibrate(longArrayOf(0, 500, 250, 500))
        builder.setCategory(NotificationCompat.CATEGORY_REMINDER)
        
        // Add snooze action button only if enabled
        if (reminderId > 0 && snoozeEnabled) {
            builder.addAction(
                android.R.drawable.ic_lock_idle_alarm,
                "Snooze 10min",
                snoozePendingIntent
            )
        }

        val notification = builder.build()

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(reminderId.toInt(), notification)
    }

    /**
     * Check if notification permission is granted (for Android 13+)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Permission not required for Android 12 and below
            true
        }
    }
}