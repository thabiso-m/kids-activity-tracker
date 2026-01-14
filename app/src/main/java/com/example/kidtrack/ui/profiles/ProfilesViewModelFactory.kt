package com.example.kidtrack.ui.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kidtrack.data.repository.KidTrackRepository

class ProfilesViewModelFactory(
    private val repository: KidTrackRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfilesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfilesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
