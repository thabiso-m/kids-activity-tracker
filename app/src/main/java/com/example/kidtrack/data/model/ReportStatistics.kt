package com.example.kidtrack.data.model

data class ReportStatistics(
    val totalActivities: Int = 0,
    val completedActivities: Int = 0,
    val thisWeekActivities: Int = 0,
    val completionRate: Int = 0,
    val categoryBreakdown: Map<String, Int> = emptyMap(),
    val recentActivities: List<Activity> = emptyList()
)
