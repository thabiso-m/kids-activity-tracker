package com.example.kidtrack.utils

import com.example.kidtrack.data.model.PredefinedTask

/**
 * Object containing predefined task templates for quick activity creation
 */
object PredefinedTasks {
    
    val ALL_TASKS = listOf(
        // School Related
        PredefinedTask(
            category = "School",
            description = "Homework",
            suggestedTimeMinutes = 960 // 4:00 PM
        ),
        PredefinedTask(
            category = "School",
            description = "School Drop-off",
            suggestedTimeMinutes = 450 // 7:30 AM
        ),
        PredefinedTask(
            category = "School",
            description = "School Pick-up",
            suggestedTimeMinutes = 900 // 3:00 PM
        ),
        PredefinedTask(
            category = "School",
            description = "Parent-Teacher Meeting",
            suggestedTimeMinutes = 960 // 4:00 PM
        ),
        PredefinedTask(
            category = "School",
            description = "School Project Due",
            suggestedTimeMinutes = 540 // 9:00 AM
        ),
        
        // Activities & Hobbies
        PredefinedTask(
            category = "Activity",
            description = "Soccer Practice",
            suggestedTimeMinutes = 1020 // 5:00 PM
        ),
        PredefinedTask(
            category = "Activity",
            description = "Swimming Lesson",
            suggestedTimeMinutes = 960 // 4:00 PM
        ),
        PredefinedTask(
            category = "Activity",
            description = "Music Lesson",
            suggestedTimeMinutes = 960 // 4:00 PM
        ),
        PredefinedTask(
            category = "Activity",
            description = "Dance Class",
            suggestedTimeMinutes = 1020 // 5:00 PM
        ),
        PredefinedTask(
            category = "Activity",
            description = "Art Class",
            suggestedTimeMinutes = 960 // 4:00 PM
        ),
        
        // Health & Medical
        PredefinedTask(
            category = "Health",
            description = "Doctor Appointment",
            suggestedTimeMinutes = 600 // 10:00 AM
        ),
        PredefinedTask(
            category = "Health",
            description = "Dentist Appointment",
            suggestedTimeMinutes = 600 // 10:00 AM
        ),
        PredefinedTask(
            category = "Health",
            description = "Vaccination",
            suggestedTimeMinutes = 540 // 9:00 AM
        ),
        PredefinedTask(
            category = "Health",
            description = "Medicine Time",
            suggestedTimeMinutes = 480 // 8:00 AM
        ),
        
        // Daily Routines
        PredefinedTask(
            category = "Routine",
            description = "Bedtime",
            suggestedTimeMinutes = 1200 // 8:00 PM
        ),
        PredefinedTask(
            category = "Routine",
            description = "Wake Up Time",
            suggestedTimeMinutes = 420 // 7:00 AM
        ),
        PredefinedTask(
            category = "Routine",
            description = "Meal Time",
            suggestedTimeMinutes = 720 // 12:00 PM
        ),
        PredefinedTask(
            category = "Routine",
            description = "Bath Time",
            suggestedTimeMinutes = 1140 // 7:00 PM
        ),
        PredefinedTask(
            category = "Routine",
            description = "Story Time",
            suggestedTimeMinutes = 1170 // 7:30 PM
        ),
        
        // Social & Events
        PredefinedTask(
            category = "Social",
            description = "Playdate",
            suggestedTimeMinutes = 960 // 4:00 PM
        ),
        PredefinedTask(
            category = "Social",
            description = "Birthday Party",
            suggestedTimeMinutes = 900 // 3:00 PM
        ),
        PredefinedTask(
            category = "Social",
            description = "Family Outing",
            suggestedTimeMinutes = 600 // 10:00 AM
        ),
        
        // Chores & Responsibilities
        PredefinedTask(
            category = "Chore",
            description = "Clean Room",
            suggestedTimeMinutes = 1020 // 5:00 PM
        ),
        PredefinedTask(
            category = "Chore",
            description = "Help with Dishes",
            suggestedTimeMinutes = 1110 // 6:30 PM
        ),
        PredefinedTask(
            category = "Chore",
            description = "Feed Pet",
            suggestedTimeMinutes = 480 // 8:00 AM
        )
    )
    
    /**
     * Get predefined tasks grouped by category
     */
    fun getTasksByCategory(): Map<String, List<PredefinedTask>> {
        return ALL_TASKS.groupBy { it.category }
    }
    
    /**
     * Get all unique categories
     */
    fun getCategories(): List<String> {
        return ALL_TASKS.map { it.category }.distinct().sorted()
    }
    
    /**
     * Get tasks for a specific category
     */
    fun getTasksForCategory(category: String): List<PredefinedTask> {
        return ALL_TASKS.filter { it.category == category }
    }
    
    /**
     * Get all task descriptions (for dropdown/autocomplete)
     */
    fun getAllDescriptions(): List<String> {
        return ALL_TASKS.map { it.description }
    }
}
