package com.example.kidtrack.ui.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kidtrack.data.repository.KidTrackRepository

class ActivitiesViewModelFactory(
    private val repository: KidTrackRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActivitiesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ActivitiesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
