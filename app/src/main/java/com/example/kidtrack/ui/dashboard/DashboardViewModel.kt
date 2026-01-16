package com.example.kidtrack.ui.dashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidtrack.data.model.Activity
import com.example.kidtrack.data.repository.KidTrackRepository
import com.example.kidtrack.utils.UiState
import kotlinx.coroutines.launch

class DashboardViewModel(private val repository: KidTrackRepository) : ViewModel() {

    companion object {
        private const val TAG = "DashboardViewModel"
    }

    private val _upcomingActivities = MutableLiveData<UiState<List<Activity>>>()
    val upcomingActivities: LiveData<UiState<List<Activity>>> get() = _upcomingActivities

    private val _overdueTasks = MutableLiveData<UiState<List<Activity>>>()
    val overdueTasks: LiveData<UiState<List<Activity>>> get() = _overdueTasks

    /**
     * Fetch upcoming activities (today and future)
     */
    fun fetchUpcomingActivities() {
        viewModelScope.launch {
            _upcomingActivities.value = UiState.Loading
            try {
                val activities = repository.getUpcomingActivities()
                _upcomingActivities.value = UiState.Success(activities)
                Log.d(TAG, "Fetched ${activities.size} upcoming activities")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching upcoming activities", e)
                _upcomingActivities.value = UiState.Error(
                    message = "Failed to load upcoming activities: ${e.message}",
                    exception = e
                )
            }
        }
    }

    /**
     * Fetch overdue tasks (past activities)
     */
    fun fetchOverdueTasks() {
        viewModelScope.launch {
            _overdueTasks.value = UiState.Loading
            try {
                val tasks = repository.getOverdueTasks()
                _overdueTasks.value = UiState.Success(tasks)
                Log.d(TAG, "Fetched ${tasks.size} overdue tasks")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching overdue tasks", e)
                _overdueTasks.value = UiState.Error(
                    message = "Failed to load overdue tasks: ${e.message}",
                    exception = e
                )
            }
        }
    }

    /**
     * Update all dashboard data
     */
    fun updateActivities() {
        fetchUpcomingActivities()
        fetchOverdueTasks()
    }
    
    /**
     * Load all dashboard data
     */
    fun loadDashboardData() {
        updateActivities()
    }

    /**
     * Clear error state and retry loading
     */
    fun retry() {
        loadDashboardData()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared")
    }
}