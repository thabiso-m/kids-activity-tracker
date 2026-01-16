package com.example.kidtrack.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility object for date and time operations across the application.
 * Provides consistent date/time formatting and parsing.
 */
object DateTimeUtils {

    // Standard date format: yyyy-MM-dd
    private const val DATE_FORMAT = "yyyy-MM-dd"
    
    // Standard time format: HH:mm (24-hour)
    private const val TIME_FORMAT = "HH:mm"
    
    // Display date format: dd MMM yyyy (e.g., 15 Jan 2026)
    private const val DISPLAY_DATE_FORMAT = "dd MMM yyyy"
    
    // Display time format: hh:mm a (e.g., 02:30 PM)
    private const val DISPLAY_TIME_FORMAT = "hh:mm a"

    /**
     * Get current date as timestamp (milliseconds)
     */
    fun getCurrentTimestamp(): Long = System.currentTimeMillis()

    /**
     * Convert a date string (yyyy-MM-dd) to timestamp
     * @param dateString Date in yyyy-MM-dd format
     * @return Timestamp in milliseconds, or null if parsing fails
     */
    fun dateStringToTimestamp(dateString: String): Long? {
        return try {
            val format = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            format.isLenient = false
            format.parse(dateString)?.time
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Convert a timestamp to date string (yyyy-MM-dd)
     * @param timestamp Timestamp in milliseconds
     * @return Date string in yyyy-MM-dd format
     */
    fun timestampToDateString(timestamp: Long): String {
        val format = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return format.format(Date(timestamp))
    }

    /**
     * Convert a timestamp to display date string (dd MMM yyyy)
     * @param timestamp Timestamp in milliseconds
     * @return Date string in display format
     */
    fun timestampToDisplayDate(timestamp: Long): String {
        val format = SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault())
        return format.format(Date(timestamp))
    }

    /**
     * Convert a time string (HH:mm) to minutes since midnight
     * @param timeString Time in HH:mm format
     * @return Minutes since midnight, or null if parsing fails
     */
    fun timeStringToMinutes(timeString: String): Int? {
        return try {
            val parts = timeString.split(":")
            if (parts.size == 2) {
                val hours = parts[0].toInt()
                val minutes = parts[1].toInt()
                if (hours in 0..23 && minutes in 0..59) {
                    hours * 60 + minutes
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Convert minutes since midnight to time string (HH:mm)
     * @param minutes Minutes since midnight
     * @return Time string in HH:mm format
     */
    fun minutesToTimeString(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return String.format(Locale.getDefault(), "%02d:%02d", hours, mins)
    }

    /**
     * Convert time string to display format (hh:mm a)
     * @param timeString Time in HH:mm format
     * @return Time string in display format (e.g., 02:30 PM)
     */
    fun timeStringToDisplayTime(timeString: String): String {
        return try {
            val inputFormat = SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
            val outputFormat = SimpleDateFormat(DISPLAY_TIME_FORMAT, Locale.getDefault())
            val date = inputFormat.parse(timeString)
            if (date != null) outputFormat.format(date) else timeString
        } catch (e: Exception) {
            timeString
        }
    }

    /**
     * Get today's date as string (yyyy-MM-dd)
     */
    fun getTodayDateString(): String {
        return timestampToDateString(getCurrentTimestamp())
    }

    /**
     * Check if a timestamp is today
     * @param timestamp Timestamp to check
     * @return true if the timestamp is today
     */
    fun isToday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_YEAR)
        val todayYear = calendar.get(Calendar.YEAR)
        
        calendar.timeInMillis = timestamp
        val dateDay = calendar.get(Calendar.DAY_OF_YEAR)
        val dateYear = calendar.get(Calendar.YEAR)
        
        return today == dateDay && todayYear == dateYear
    }

    /**
     * Check if a timestamp is in the past (before today)
     * @param timestamp Timestamp to check
     * @return true if the timestamp is before today
     */
    fun isPast(timestamp: Long): Boolean {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        return timestamp < today
    }

    /**
     * Check if a timestamp is in the future (after today)
     * @param timestamp Timestamp to check
     * @return true if the timestamp is after today
     */
    fun isFuture(timestamp: Long): Boolean {
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        return timestamp >= tomorrow
    }

    /**
     * Get the start of week timestamp (Monday at 00:00:00)
     * @return Timestamp for the start of current week
     */
    fun getStartOfWeekTimestamp(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Get the end of week timestamp (Sunday at 23:59:59)
     * @return Timestamp for the end of current week
     */
    fun getEndOfWeekTimestamp(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    /**
     * Combine date and time strings into a single timestamp
     * @param dateString Date in yyyy-MM-dd format
     * @param timeString Time in HH:mm format
     * @return Combined timestamp, or null if parsing fails
     */
    fun combineDateTime(dateString: String, timeString: String): Long? {
        return try {
            val datePart = dateStringToTimestamp(dateString) ?: return null
            val timeParts = timeString.split(":")
            if (timeParts.size != 2) return null
            
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = datePart
            calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
            calendar.set(Calendar.MINUTE, timeParts[1].toInt())
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            calendar.timeInMillis
        } catch (e: Exception) {
            null
        }
    }
}
