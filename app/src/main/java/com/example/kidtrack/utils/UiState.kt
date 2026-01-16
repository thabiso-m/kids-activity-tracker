package com.example.kidtrack.utils

/**
 * Sealed class representing the UI state for data operations.
 * Provides a type-safe way to handle loading, success, and error states.
 */
sealed class UiState<out T> {
    /**
     * Represents a loading state where data is being fetched
     */
    object Loading : UiState<Nothing>()

    /**
     * Represents a successful state with data
     * @param data The data that was successfully loaded
     */
    data class Success<T>(val data: T) : UiState<T>()

    /**
     * Represents an error state with an error message
     * @param message The error message to display
     * @param exception The original exception (optional, for logging)
     */
    data class Error(
        val message: String,
        val exception: Throwable? = null
    ) : UiState<Nothing>()
}
