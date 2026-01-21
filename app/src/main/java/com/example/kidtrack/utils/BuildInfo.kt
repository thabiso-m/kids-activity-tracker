package com.example.kidtrack.utils

import com.example.kidtrack.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility object for accessing build and version information
 */
object BuildInfo {
    
    /**
     * App version name (e.g., "2.1.0")
     */
    val versionName: String
        get() = BuildConfig.VERSION_NAME
    
    /**
     * App version code (e.g., 4)
     */
    val versionCode: Int
        get() = BuildConfig.VERSION_CODE
    
    /**
     * Build timestamp formatted as readable date
     */
    val buildTime: String
        get() {
            val timestamp = BuildConfig.BUILD_TIME.toLongOrNull() ?: System.currentTimeMillis()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    
    /**
     * Build type (debug/release)
     */
    val buildType: String
        get() = if (BuildConfig.DEBUG) "Debug" else "Release"
    
    /**
     * Full version string with all information
     * Example: "KidTrack v2.1.0 (Build 4)"
     */
    val fullVersionString: String
        get() = "KidTrack v$versionName (Build $versionCode)"
    
    /**
     * Complete build information
     * Example: "KidTrack v2.1.0 (Build 4)\nBuilt on 2026-01-17 10:30:45"
     */
    val fullBuildInfo: String
        get() = """
            KidTrack v$versionName (Build $versionCode)
            Built on $buildTime
            Type: $buildType
        """.trimIndent()
    
    /**
     * Short version for display (e.g., "v2.1.0")
     */
    val shortVersion: String
        get() = "v$versionName"
    
    /**
     * Check if this is a debug build
     */
    val isDebug: Boolean
        get() = BuildConfig.DEBUG
}
