package com.example.kidtrack.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kidtrack.data.repository.KidTrackRepository

class RemindersViewModelFactory(
    private val repository: KidTrackRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RemindersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RemindersViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
