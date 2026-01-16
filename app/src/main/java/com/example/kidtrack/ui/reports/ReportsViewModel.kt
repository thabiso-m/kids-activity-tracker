package com.example.kidtrack.ui.reports

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidtrack.data.model.ReportStatistics
import com.example.kidtrack.data.repository.KidTrackRepository
import com.example.kidtrack.utils.UiState
import kotlinx.coroutines.launch

class ReportsViewModel(private val repository: KidTrackRepository) : ViewModel() {

    companion object {
        private const val TAG = "ReportsViewModel"
    }

    private val _statistics = MutableLiveData<UiState<ReportStatistics>>()
    val statistics: LiveData<UiState<ReportStatistics>> get() = _statistics

    private val _weeklySummary = MutableLiveData<UiState<Map<String, Any>>>()
    val weeklySummary: LiveData<UiState<Map<String, Any>>> get() = _weeklySummary

    fun loadStatistics() {
        viewModelScope.launch {
            _statistics.value = UiState.Loading
            try {
                val stats = repository.getReportStatistics()
                _statistics.value = UiState.Success(stats)
                Log.d(TAG, "Statistics loaded successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading statistics", e)
                _statistics.value = UiState.Error(
                    message = "Failed to load statistics: ${e.message}",
                    exception = e
                )
            }
        }
    }

    fun generateWeeklySummary() {
        viewModelScope.launch {
            _weeklySummary.value = UiState.Loading
            try {
                val summary = repository.getWeeklySummary()
                _weeklySummary.value = UiState.Success(summary)
                Log.d(TAG, "Weekly summary generated successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error generating weekly summary", e)
                _weeklySummary.value = UiState.Error(
                    message = "Failed to generate weekly summary: ${e.message}",
                    exception = e
                )
            }
        }
    }

    fun retry() {
        loadStatistics()
        generateWeeklySummary()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared")
    }
}