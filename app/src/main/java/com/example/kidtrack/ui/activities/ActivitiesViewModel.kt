package com.example.kidtrack.ui.activities

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidtrack.data.model.Activity
import com.example.kidtrack.data.repository.KidTrackRepository
import com.example.kidtrack.utils.UiState
import kotlinx.coroutines.launch

class ActivitiesViewModel(private val repository: KidTrackRepository) : ViewModel() {

    companion object {
        private const val TAG = "ActivitiesViewModel"
    }

    private val _activities = MutableLiveData<UiState<List<Activity>>>()
    val activities: LiveData<UiState<List<Activity>>> get() = _activities

    private val _operationStatus = MutableLiveData<UiState<String>>()
    val operationStatus: LiveData<UiState<String>> get() = _operationStatus

    fun addActivity(activity: Activity) {
        viewModelScope.launch {
            _operationStatus.value = UiState.Loading
            try {
                repository.insertActivity(activity)
                _operationStatus.value = UiState.Success("Activity added successfully")
                fetchActivities()
                Log.d(TAG, "Activity added: ${activity.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding activity", e)
                _operationStatus.value = UiState.Error(
                    message = "Failed to add activity: ${e.message}",
                    exception = e
                )
            }
        }
    }

    fun updateActivity(activity: Activity) {
        viewModelScope.launch {
            _operationStatus.value = UiState.Loading
            try {
                repository.insertActivity(activity)
                _operationStatus.value = UiState.Success("Activity updated successfully")
                fetchActivities()
                Log.d(TAG, "Activity updated: ${activity.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating activity", e)
                _operationStatus.value = UiState.Error(
                    message = "Failed to update activity: ${e.message}",
                    exception = e
                )
            }
        }
    }

    fun deleteActivity(activity: Activity) {
        viewModelScope.launch {
            _operationStatus.value = UiState.Loading
            try {
                repository.deleteActivity(activity)
                _operationStatus.value = UiState.Success("Activity deleted successfully")
                fetchActivities()
                Log.d(TAG, "Activity deleted: ${activity.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting activity", e)
                _operationStatus.value = UiState.Error(
                    message = "Failed to delete activity: ${e.message}",
                    exception = e
                )
            }
        }
    }

    fun fetchActivities() {
        viewModelScope.launch {
            _activities.value = UiState.Loading
            try {
                val activitiesList = repository.getAllActivities()
                _activities.value = UiState.Success(activitiesList)
                Log.d(TAG, "Fetched ${activitiesList.size} activities")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching activities", e)
                _activities.value = UiState.Error(
                    message = "Failed to load activities: ${e.message}",
                    exception = e
                )
            }
        }
    }

    fun retry() {
        fetchActivities()
    }

    fun clearOperationStatus() {
        _operationStatus.value = null
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared")
    }
}