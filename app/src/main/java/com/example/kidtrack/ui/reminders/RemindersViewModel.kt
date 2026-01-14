package com.example.kidtrack.ui.reminders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidtrack.data.model.Reminder
import com.example.kidtrack.data.repository.KidTrackRepository
import kotlinx.coroutines.launch

class RemindersViewModel(private val repository: KidTrackRepository) : ViewModel() {

    private val _reminders = MutableLiveData<List<Reminder>>()
    val reminders: LiveData<List<Reminder>> get() = _reminders

    fun fetchReminders() {
        viewModelScope.launch {
            _reminders.value = repository.getAllReminders()
        }
    }

    fun addReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.insertReminder(reminder)
            fetchReminders()
        }
    }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.insertReminder(reminder)
            fetchReminders()
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            fetchReminders()
        }
    }

    fun removeReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            fetchReminders()
        }
    }
}