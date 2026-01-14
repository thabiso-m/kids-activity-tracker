package com.example.kidtrack.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidtrack.data.model.Activity
import com.example.kidtrack.data.repository.KidTrackRepository
import kotlinx.coroutines.launch

class DashboardViewModel(private val repository: KidTrackRepository) : ViewModel() {

    private val _upcomingActivities = MutableLiveData<List<Activity>>()
    val upcomingActivities: LiveData<List<Activity>> get() = _upcomingActivities

    private val _overdueTasks = MutableLiveData<List<Activity>>()
    val overdueTasks: LiveData<List<Activity>> get() = _overdueTasks

    fun fetchUpcomingActivities() {
        viewModelScope.launch {
            _upcomingActivities.value = repository.getUpcomingActivities()
        }
    }

    fun fetchOverdueTasks() {
        viewModelScope.launch {
            _overdueTasks.value = repository.getOverdueTasks()
        }
    }

    fun updateActivities() {
        fetchUpcomingActivities()
        fetchOverdueTasks()
    }
    
    fun loadDashboardData() {
        updateActivities()
    }
}