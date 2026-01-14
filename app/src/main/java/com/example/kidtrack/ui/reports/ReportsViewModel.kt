package com.example.kidtrack.ui.reports

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidtrack.data.model.ReportStatistics
import com.example.kidtrack.data.repository.KidTrackRepository
import kotlinx.coroutines.launch

class ReportsViewModel(private val repository: KidTrackRepository) : ViewModel() {

    private val _statistics = MutableLiveData<ReportStatistics>()
    val statistics: LiveData<ReportStatistics> get() = _statistics

    private val _weeklySummary = MutableLiveData<Map<String, Any>>()
    val weeklySummary: LiveData<Map<String, Any>> get() = _weeklySummary

    fun loadStatistics() {
        viewModelScope.launch {
            _statistics.value = repository.getReportStatistics()
        }
    }

    fun generateWeeklySummary() {
        viewModelScope.launch {
            _weeklySummary.value = repository.getWeeklySummary()
        }
    }
}