package com.example.kidtrack.ui.reminders

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidtrack.data.model.Reminder
import com.example.kidtrack.data.repository.KidTrackRepository
import com.example.kidtrack.utils.UiState
import kotlinx.coroutines.launch

class RemindersViewModel(private val repository: KidTrackRepository) : ViewModel() {

    companion object {
        private const val TAG = "RemindersViewModel"
    }

    private val _reminders = MutableLiveData<UiState<List<Reminder>>>()
    val reminders: LiveData<UiState<List<Reminder>>> get() = _reminders

    private val _operationStatus = MutableLiveData<UiState<String>>()
    val operationStatus: LiveData<UiState<String>> get() = _operationStatus

    fun fetchReminders() {
        viewModelScope.launch {
            _reminders.value = UiState.Loading
            try {
                val remindersList = repository.getAllReminders()
                _reminders.value = UiState.Success(remindersList)
                Log.d(TAG, "Fetched ${remindersList.size} reminders")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching reminders", e)
                _reminders.value = UiState.Error(
                    message = "Failed to load reminders: ${e.message}",
                    exception = e
                )
            }
        }
    }

    fun addReminder(reminder: Reminder) {
        viewModelScope.launch {
            _operationStatus.value = UiState.Loading
            try {
                repository.insertReminder(reminder)
                _operationStatus.value = UiState.Success("Reminder added successfully")
                fetchReminders()
                Log.d(TAG, "Reminder added: ${reminder.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding reminder", e)
                _operationStatus.value = UiState.Error(
                    message = "Failed to add reminder: ${e.message}",
                    exception = e
                )
            }
        }
    }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            _operationStatus.value = UiState.Loading
            try {
                repository.insertReminder(reminder)
                _operationStatus.value = UiState.Success("Reminder updated successfully")
                fetchReminders()
                Log.d(TAG, "Reminder updated: ${reminder.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating reminder", e)
                _operationStatus.value = UiState.Error(
                    message = "Failed to update reminder: ${e.message}",
                    exception = e
                )
            }
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            _operationStatus.value = UiState.Loading
            try {
                repository.deleteReminder(reminder)
                _operationStatus.value = UiState.Success("Reminder deleted successfully")
                fetchReminders()
                Log.d(TAG, "Reminder deleted: ${reminder.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting reminder", e)
                _operationStatus.value = UiState.Error(
                    message = "Failed to delete reminder: ${e.message}",
                    exception = e
                )
            }
        }
    }

    fun removeReminder(reminder: Reminder) {
        deleteReminder(reminder)
    }

    fun retry() {
        fetchReminders()
    }

    fun clearOperationStatus() {
        _operationStatus.value = null
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared")
    }
}