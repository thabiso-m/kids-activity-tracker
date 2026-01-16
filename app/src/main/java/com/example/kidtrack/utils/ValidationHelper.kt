package com.example.kidtrack.utils

import android.util.Patterns
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility object for validating user input across the application.
 * Provides consistent validation rules and error messages.
 */
object ValidationHelper {

    /**
     * Validates that a string is not empty or blank
     * @param value The string to validate
     * @param fieldName The name of the field being validated (for error message)
     * @return ValidationResult with success or error message
     */
    fun validateNotEmpty(value: String, fieldName: String): ValidationResult {
        return if (value.isBlank()) {
            ValidationResult.Error("$fieldName cannot be empty")
        } else {
            ValidationResult.Success
        }
    }

    /**
     * Validates that a string has a minimum length
     * @param value The string to validate
     * @param minLength Minimum required length
     * @param fieldName The name of the field being validated
     * @return ValidationResult with success or error message
     */
    fun validateMinLength(value: String, minLength: Int, fieldName: String): ValidationResult {
        return if (value.length < minLength) {
            ValidationResult.Error("$fieldName must be at least $minLength characters")
        } else {
            ValidationResult.Success
        }
    }

    /**
     * Validates that a string has a maximum length
     * @param value The string to validate
     * @param maxLength Maximum allowed length
     * @param fieldName The name of the field being validated
     * @return ValidationResult with success or error message
     */
    fun validateMaxLength(value: String, maxLength: Int, fieldName: String): ValidationResult {
        return if (value.length > maxLength) {
            ValidationResult.Error("$fieldName cannot exceed $maxLength characters")
        } else {
            ValidationResult.Success
        }
    }

    /**
     * Validates a date string in yyyy-MM-dd format
     * @param dateString The date string to validate
     * @return ValidationResult with success or error message
     */
    fun validateDate(dateString: String): ValidationResult {
        if (dateString.isBlank()) {
            return ValidationResult.Error("Date cannot be empty")
        }

        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            format.isLenient = false
            format.parse(dateString)
            ValidationResult.Success
        } catch (e: Exception) {
            ValidationResult.Error("Invalid date format. Use yyyy-MM-dd")
        }
    }

    /**
     * Validates a time string in HH:mm format (24-hour)
     * @param timeString The time string to validate
     * @return ValidationResult with success or error message
     */
    fun validateTime(timeString: String): ValidationResult {
        if (timeString.isBlank()) {
            return ValidationResult.Error("Time cannot be empty")
        }

        return try {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            format.isLenient = false
            format.parse(timeString)
            ValidationResult.Success
        } catch (e: Exception) {
            ValidationResult.Error("Invalid time format. Use HH:mm")
        }
    }

    /**
     * Validates that a date is not in the past
     * @param dateString The date string to validate (yyyy-MM-dd)
     * @return ValidationResult with success or error message
     */
    fun validateDateNotInPast(dateString: String): ValidationResult {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            format.isLenient = false
            val date = format.parse(dateString)
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            if (date != null && date.before(today)) {
                ValidationResult.Error("Date cannot be in the past")
            } else {
                ValidationResult.Success
            }
        } catch (e: Exception) {
            ValidationResult.Error("Invalid date")
        }
    }

    /**
     * Validates an age value
     * @param age The age to validate
     * @return ValidationResult with success or error message
     */
    fun validateAge(age: Int): ValidationResult {
        return when {
            age < 0 -> ValidationResult.Error("Age cannot be negative")
            age > 120 -> ValidationResult.Error("Age must be realistic")
            age == 0 -> ValidationResult.Error("Age must be greater than 0")
            else -> ValidationResult.Success
        }
    }

    /**
     * Validates an email address
     * @param email The email to validate
     * @return ValidationResult with success or error message
     */
    fun validateEmail(email: String): ValidationResult {
        return if (email.isBlank()) {
            ValidationResult.Error("Email cannot be empty")
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ValidationResult.Error("Invalid email address")
        } else {
            ValidationResult.Success
        }
    }

    /**
     * Validates a profile ID
     * @param profileId The profile ID to validate
     * @return ValidationResult with success or error message
     */
    fun validateProfileId(profileId: Long): ValidationResult {
        return if (profileId <= 0) {
            ValidationResult.Error("Please select a valid profile")
        } else {
            ValidationResult.Success
        }
    }

    /**
     * Combines multiple validation results
     * @param validations List of validation results to combine
     * @return The first error found, or Success if all validations pass
     */
    fun combine(vararg validations: ValidationResult): ValidationResult {
        validations.forEach { validation ->
            if (validation is ValidationResult.Error) {
                return validation
            }
        }
        return ValidationResult.Success
    }
}

/**
 * Sealed class representing the result of a validation operation
 */
sealed class ValidationResult {
    /**
     * Validation succeeded
     */
    object Success : ValidationResult()

    /**
     * Validation failed with an error message
     * @param message The error message describing why validation failed
     */
    data class Error(val message: String) : ValidationResult()

    /**
     * Check if the validation was successful
     */
    fun isSuccess(): Boolean = this is Success

    /**
     * Get the error message if validation failed, null otherwise
     */
    fun getErrorMessage(): String? = (this as? Error)?.message
}
