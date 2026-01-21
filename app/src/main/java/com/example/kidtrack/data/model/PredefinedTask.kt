package com.example.kidtrack.data.model

/**
 * Data class representing a predefined task template
 * that users can quickly select when creating activities
 */
data class PredefinedTask(
    val category: String,
    val description: String,
    val suggestedTimeMinutes: Int? = null, // Optional suggested time (e.g., 480 = 8:00 AM)
    val icon: String? = null // Optional icon identifier
)
