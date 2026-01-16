package com.example.kidtrack.ui.profiles

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidtrack.data.model.UserProfile
import com.example.kidtrack.data.repository.KidTrackRepository
import com.example.kidtrack.utils.UiState
import kotlinx.coroutines.launch

class ProfilesViewModel(private val repository: KidTrackRepository) : ViewModel() {

    companion object {
        private const val TAG = "ProfilesViewModel"
    }

    private val _profiles = MutableLiveData<UiState<List<UserProfile>>>()
    val profiles: LiveData<UiState<List<UserProfile>>> get() = _profiles

    private val _operationStatus = MutableLiveData<UiState<String>>()
    val operationStatus: LiveData<UiState<String>> get() = _operationStatus

    fun fetchProfiles() {
        viewModelScope.launch {
            _profiles.value = UiState.Loading
            try {
                val profilesList = repository.getAllUserProfiles()
                _profiles.value = UiState.Success(profilesList)
                Log.d(TAG, "Fetched ${profilesList.size} profiles")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching profiles", e)
                _profiles.value = UiState.Error(
                    message = "Failed to load profiles: ${e.message}",
                    exception = e
                )
            }
        }
    }

    fun addProfile(profile: UserProfile) {
        viewModelScope.launch {
            _operationStatus.value = UiState.Loading
            try {
                repository.insertUserProfile(profile)
                _operationStatus.value = UiState.Success("Profile added successfully")
                fetchProfiles()
                Log.d(TAG, "Profile added: ${profile.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding profile", e)
                _operationStatus.value = UiState.Error(
                    message = "Failed to add profile: ${e.message}",
                    exception = e
                )
            }
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            _operationStatus.value = UiState.Loading
            try {
                repository.insertUserProfile(profile)
                _operationStatus.value = UiState.Success("Profile updated successfully")
                fetchProfiles()
                Log.d(TAG, "Profile updated: ${profile.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating profile", e)
                _operationStatus.value = UiState.Error(
                    message = "Failed to update profile: ${e.message}",
                    exception = e
                )
            }
        }
    }

    fun deleteProfile(profile: UserProfile) {
        viewModelScope.launch {
            _operationStatus.value = UiState.Loading
            try {
                repository.deleteUserProfile(profile)
                _operationStatus.value = UiState.Success("Profile and associated data deleted successfully")
                fetchProfiles()
                Log.d(TAG, "Profile deleted: ${profile.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting profile", e)
                _operationStatus.value = UiState.Error(
                    message = "Failed to delete profile: ${e.message}",
                    exception = e
                )
            }
        }
    }

    fun retry() {
        fetchProfiles()
    }

    fun clearOperationStatus() {
        _operationStatus.value = null
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared")
    }
}