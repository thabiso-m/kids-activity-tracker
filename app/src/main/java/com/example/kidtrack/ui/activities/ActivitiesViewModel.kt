package com.example.kidtrack.ui.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidtrack.data.model.Activity
import com.example.kidtrack.data.repository.KidTrackRepository
import kotlinx.coroutines.launch

class ActivitiesViewModel(private val repository: KidTrackRepository) : ViewModel() {

    private val _activities = MutableLiveData<List<Activity>>()
    val activities: LiveData<List<Activity>> get() = _activities

    fun addActivity(activity: Activity) {
        viewModelScope.launch {
            repository.insertActivity(activity)
            fetchActivities()
        }
    }

    fun updateActivity(activity: Activity) {
        viewModelScope.launch {
            repository.insertActivity(activity)
            fetchActivities()
        }
    }

    fun deleteActivity(activity: Activity) {
        viewModelScope.launch {
            repository.deleteActivity(activity)
            fetchActivities()
        }
    }

    fun fetchActivities() {
        viewModelScope.launch {
            _activities.value = repository.getAllActivities()
        }
    }
}