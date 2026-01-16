package com.example.kidtrack.data.database.converters

import androidx.room.TypeConverter
import java.util.*

/**
 * Type converter for Room database to convert Date objects to Long (timestamp)
 * and vice versa. This ensures proper date storage and retrieval from the database.
 */
class DateConverter {

    /**
     * Convert a timestamp to a Date object
     * @param value The timestamp in milliseconds (nullable)
     * @return Date object or null
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    /**
     * Convert a Date object to a timestamp
     * @param date The Date object (nullable)
     * @return Timestamp in milliseconds or null
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
