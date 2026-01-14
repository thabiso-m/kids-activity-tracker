package com.example.kidtrack.ui.profiles

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidtrack.data.model.UserProfile
import com.example.kidtrack.data.repository.KidTrackRepository
import kotlinx.coroutines.launch

class ProfilesViewModel(private val repository: KidTrackRepository) : ViewModel() {

    private val _profiles = MutableLiveData<List<UserProfile>>()
    val profiles: LiveData<List<UserProfile>> get() = _profiles

    fun fetchProfiles() {
        viewModelScope.launch {
            _profiles.value = repository.getAllUserProfiles()
        }
    }

    fun addProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.insertUserProfile(profile)
            fetchProfiles()
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.insertUserProfile(profile)
            fetchProfiles()
        }
    }

    fun deleteProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.deleteUserProfile(profile)
            fetchProfiles()
        }
    }
}