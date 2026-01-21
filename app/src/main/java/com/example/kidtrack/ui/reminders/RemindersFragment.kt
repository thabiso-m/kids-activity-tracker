package com.example.kidtrack.ui.reminders

import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kidtrack.R
import com.example.kidtrack.data.database.KidTrackDatabase
import com.example.kidtrack.data.model.Reminder
import com.example.kidtrack.data.repository.KidTrackRepository
import com.example.kidtrack.utils.DateTimeUtils
import com.example.kidtrack.utils.ReminderScheduler
import com.example.kidtrack.utils.UiState
import com.example.kidtrack.utils.ValidationHelper
import com.example.kidtrack.utils.ValidationResult
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class RemindersFragment : Fragment() {

    private lateinit var remindersViewModel: RemindersViewModel
    private lateinit var remindersAdapter: RemindersAdapter
    private lateinit var remindersRecyclerView: RecyclerView
    private lateinit var addReminderButton: ExtendedFloatingActionButton
    private lateinit var emptyStateLayout: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val database = KidTrackDatabase.getDatabase(requireContext())
        val repository = KidTrackRepository(database)
        val factory = RemindersViewModelFactory(repository)
        remindersViewModel = ViewModelProvider(this, factory).get(RemindersViewModel::class.java)
        
        return inflater.inflate(R.layout.fragment_reminders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize RecyclerView
        remindersRecyclerView = view.findViewById(R.id.reminders_recycler_view)
        remindersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        // Initialize adapter with click listener
        remindersAdapter = RemindersAdapter(
            onItemClick = { reminder -> showEditReminderDialog(reminder) },
            onItemLongClick = { reminder -> showDeleteConfirmation(reminder) }
        )
        remindersRecyclerView.adapter = remindersAdapter
        
        // Setup empty state
        emptyStateLayout = view.findViewById(R.id.emptyStateReminders)
        setupEmptyState()
        
        // Observe reminders data with UiState
        remindersViewModel.reminders.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    remindersRecyclerView.visibility = View.GONE
                    emptyStateLayout.visibility = View.GONE
                }
                is UiState.Success -> {
                    remindersAdapter.submitList(state.data)
                    updateEmptyState(state.data.isEmpty())
                }
                is UiState.Error -> {
                    Snackbar.make(view, state.message, Snackbar.LENGTH_LONG)
                        .setAction("Retry") { remindersViewModel.retry() }
                        .show()
                }
            }
        }
        
        // Observe operation status
        remindersViewModel.operationStatus.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()
                    remindersViewModel.clearOperationStatus()
                }
                is UiState.Error -> {
                    Snackbar.make(view, state.message, Snackbar.LENGTH_LONG).show()
                    remindersViewModel.clearOperationStatus()
                }
                else -> {}
            }
        }
        
        // Setup FAB click listener
        addReminderButton = view.findViewById(R.id.add_reminder_button)
        addReminderButton.setOnClickListener {
            showAddReminderDialog()
        }
        
        // Fetch reminders from database
        remindersViewModel.fetchReminders()
    }

    private fun setupEmptyState() {
        emptyStateLayout.findViewById<ImageView>(R.id.emptyStateIcon)
            .setImageResource(android.R.drawable.ic_menu_recent_history)
        emptyStateLayout.findViewById<TextView>(R.id.emptyStateTitle).text = 
            "No Reminders Set"
        emptyStateLayout.findViewById<TextView>(R.id.emptyStateMessage).text = 
            "Create reminders to never miss important activities and tasks!"
        emptyStateLayout.findViewById<MaterialButton>(R.id.emptyStateButton).apply {
            text = "Create Reminder"
            setOnClickListener { showAddReminderDialog() }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        remindersRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showAddReminderDialog() {
        // Fetch activities and profiles from repository
        val database = KidTrackDatabase.getDatabase(requireContext())
        val repository = KidTrackRepository(database)
        
        // Use coroutine to fetch data
        viewLifecycleOwner.lifecycleScope.launch {
            val profiles = repository.getAllUserProfiles()
            val activities = repository.getAllActivities()
            
            if (profiles.isEmpty()) {
                Toast.makeText(requireContext(), "Please create a child profile first", Toast.LENGTH_LONG).show()
                return@launch
            }
            
            if (activities.isEmpty()) {
                Toast.makeText(requireContext(), "Please create an activity first", Toast.LENGTH_LONG).show()
                return@launch
            }
            
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_reminder, null)

            val profileInput = dialogView.findViewById<AutoCompleteTextView>(R.id.profileInput)
            val reminderNameInput = dialogView.findViewById<TextInputEditText>(R.id.reminderNameInput)
            val timeInput = dialogView.findViewById<TextInputEditText>(R.id.reminderTimeInput)
            val frequencyInput = dialogView.findViewById<AutoCompleteTextView>(R.id.frequencyInput)
            val activityInput = dialogView.findViewById<AutoCompleteTextView>(R.id.activityInput)
            val enableSnoozeCheckbox = dialogView.findViewById<MaterialCheckBox>(R.id.enableSnoozeCheckbox)
            
            // Setup profile dropdown
            val profileMap = profiles.associate { "${it.name} (${it.age} years)" to it.id }
            val profileNames = profileMap.keys.toList()
            val profileAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, profileNames)
            profileInput.setAdapter(profileAdapter)

            // Setup frequency dropdown
            val frequencies = resources.getStringArray(R.array.reminder_frequencies)
            val frequencyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, frequencies)
            frequencyInput.setAdapter(frequencyAdapter)
            
            // Setup activity dropdown with activity descriptions
            val activityMap = activities.associate { "${it.category} - ${it.description}" to it.id }
            val activityNames = activityMap.keys.toList()
            val activityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, activityNames)
            activityInput.setAdapter(activityAdapter)

            val calendar = Calendar.getInstance()

            // Time picker
            timeInput.setOnClickListener {
                TimePickerDialog(
                    requireContext(),
                    { _, hour, minute ->
                        timeInput.setText(String.format("%02d:%02d", hour, minute))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            }

            AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Add") { _, _ ->
                    val selectedProfile = profileInput.text.toString().trim()
                    val reminderName = reminderNameInput.text.toString().trim()
                    val time = timeInput.text.toString().trim()
                    val frequency = frequencyInput.text.toString().trim()
                    val selectedActivity = activityInput.text.toString().trim()
                    val snoozeEnabled = enableSnoozeCheckbox.isChecked

                    // Validate inputs
                    val nameValidation = ValidationHelper.validateNotEmpty(reminderName, "Reminder name")
                    val timeValidation = ValidationHelper.validateTime(time)
                    val frequencyValidation = ValidationHelper.validateNotEmpty(frequency, "Frequency")
                    val profileValidation = if (selectedProfile.isEmpty()) {
                        ValidationResult.Error("Please select a profile")
                    } else ValidationResult.Success
                    val activityValidation = if (selectedActivity.isEmpty()) {
                        ValidationResult.Error("Please select an activity")
                    } else ValidationResult.Success

                    val combinedValidation = ValidationHelper.combine(
                        nameValidation,
                        timeValidation,
                        frequencyValidation,
                        profileValidation,
                        activityValidation
                    )

                    if (!combinedValidation.isSuccess()) {
                        Toast.makeText(requireContext(), combinedValidation.getErrorMessage(), Toast.LENGTH_LONG).show()
                        return@setPositiveButton
                    }
                    
                    val profileId = profileMap[selectedProfile] ?: 0L
                    val activityId = activityMap[selectedActivity] ?: 0L
                    
                    // Convert time to minutes
                    val timeMinutes = DateTimeUtils.timeStringToMinutes(time)
                    if (timeMinutes == null) {
                        Toast.makeText(requireContext(), "Invalid time format", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    val reminder = Reminder(
                        name = reminderName,
                        timeMinutes = timeMinutes,
                        frequency = frequency,
                        associatedActivityId = activityId,
                        profileId = profileId,
                        snoozeEnabled = snoozeEnabled
                    )
                    remindersViewModel.addReminder(reminder)
                    
                    // Schedule the notification
                    viewLifecycleOwner.lifecycleScope.launch {
                        // Wait a bit for the reminder to be inserted and get its ID
                        kotlinx.coroutines.delay(100)
                        val allReminders = repository.getAllReminders()
                        val newReminder = allReminders.maxByOrNull { it.id }
                        if (newReminder != null) {
                            ReminderScheduler.scheduleReminder(requireContext(), newReminder)
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showEditReminderDialog(reminder: Reminder) {
        // Fetch activities and profiles from repository to populate dropdown
        val database = KidTrackDatabase.getDatabase(requireContext())
        val repository = KidTrackRepository(database)
        
        viewLifecycleOwner.lifecycleScope.launch {
            val profiles = repository.getAllUserProfiles()
            val activities = repository.getAllActivities()
            
            if (profiles.isEmpty()) {
                Toast.makeText(requireContext(), "No profiles available", Toast.LENGTH_LONG).show()
                return@launch
            }
            
            if (activities.isEmpty()) {
                Toast.makeText(requireContext(), "No activities available", Toast.LENGTH_LONG).show()
                return@launch
            }
            
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_reminder, null)

            val profileInput = dialogView.findViewById<AutoCompleteTextView>(R.id.profileInput)
            val reminderNameInput = dialogView.findViewById<TextInputEditText>(R.id.reminderNameInput)
            val timeInput = dialogView.findViewById<TextInputEditText>(R.id.reminderTimeInput)
            val frequencyInput = dialogView.findViewById<AutoCompleteTextView>(R.id.frequencyInput)
            val activityInput = dialogView.findViewById<AutoCompleteTextView>(R.id.activityInput)
            val enableSnoozeCheckbox = dialogView.findViewById<MaterialCheckBox>(R.id.enableSnoozeCheckbox)

            // Pre-fill fields
            reminderNameInput.setText(reminder.name)
            timeInput.setText(DateTimeUtils.minutesToTimeString(reminder.timeMinutes))
            enableSnoozeCheckbox.isChecked = reminder.snoozeEnabled
            
            // Setup profile dropdown
            val profileMap = profiles.associate { "${it.name} (${it.age} years)" to it.id }
            val profileNames = profileMap.keys.toList()
            val profileAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, profileNames)
            profileInput.setAdapter(profileAdapter)
            
            // Pre-select current profile
            val currentProfile = profiles.find { it.id == reminder.profileId }
            if (currentProfile != null) {
                profileInput.setText("${currentProfile.name} (${currentProfile.age} years)", false)
            }

            // Setup frequency dropdown
            val frequencies = resources.getStringArray(R.array.reminder_frequencies)
            val frequencyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, frequencies)
            frequencyInput.setAdapter(frequencyAdapter)
            frequencyInput.setText(reminder.frequency, false)
            
            // Setup activity dropdown
            val activityMap = activities.associate { "${it.category} - ${it.description}" to it.id }
            val activityNames = activityMap.keys.toList()
            val activityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, activityNames)
            activityInput.setAdapter(activityAdapter)
            
            // Pre-select current activity
            val currentActivity = activities.find { it.id == reminder.associatedActivityId }
            if (currentActivity != null) {
                activityInput.setText("${currentActivity.category} - ${currentActivity.description}", false)
            }

            val calendar = Calendar.getInstance()

            // Time picker
            timeInput.setOnClickListener {
                TimePickerDialog(
                    requireContext(),
                    { _, hour, minute ->
                        timeInput.setText(String.format("%02d:%02d", hour, minute))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Edit Reminder")
                .setView(dialogView)
                .setPositiveButton("Update") { _, _ ->
                    val selectedProfile = profileInput.text.toString().trim()
                    val reminderName = reminderNameInput.text.toString().trim()
                    val time = timeInput.text.toString().trim()
                    val frequency = frequencyInput.text.toString().trim()
                    val selectedActivity = activityInput.text.toString().trim()
                    val snoozeEnabled = enableSnoozeCheckbox.isChecked

                    // Validate inputs
                    val nameValidation = ValidationHelper.validateNotEmpty(reminderName, "Reminder name")
                    val timeValidation = ValidationHelper.validateTime(time)
                    val frequencyValidation = ValidationHelper.validateNotEmpty(frequency, "Frequency")
                    val profileValidation = if (selectedProfile.isEmpty()) {
                        ValidationResult.Error("Please select a profile")
                    } else ValidationResult.Success
                    val activityValidation = if (selectedActivity.isEmpty()) {
                        ValidationResult.Error("Please select an activity")
                    } else ValidationResult.Success

                    val combinedValidation = ValidationHelper.combine(
                        nameValidation,
                        timeValidation,
                        frequencyValidation,
                        profileValidation,
                        activityValidation
                    )

                    if (!combinedValidation.isSuccess()) {
                        Toast.makeText(requireContext(), combinedValidation.getErrorMessage(), Toast.LENGTH_LONG).show()
                        return@setPositiveButton
                    }
                    
                    val profileId = profileMap[selectedProfile] ?: reminder.profileId
                    val activityId = activityMap[selectedActivity] ?: reminder.associatedActivityId
                    
                    // Convert time to minutes
                    val timeMinutes = DateTimeUtils.timeStringToMinutes(time)
                    if (timeMinutes == null) {
                        Toast.makeText(requireContext(), "Invalid time format", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    
                    val updatedReminder = reminder.copy(
                        name = reminderName,
                        profileId = profileId,
                        associatedActivityId = activityId,
                        timeMinutes = timeMinutes,
                        frequency = frequency,
                        snoozeEnabled = snoozeEnabled
                    )
                    remindersViewModel.updateReminder(updatedReminder)
                    
                    // Cancel old alarm and schedule new one
                    ReminderScheduler.cancelReminder(requireContext(), reminder.id)
                    ReminderScheduler.scheduleReminder(requireContext(), updatedReminder)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showDeleteConfirmation(reminder: Reminder) {
        val timeDisplay = DateTimeUtils.minutesToTimeString(reminder.timeMinutes)
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Reminder")
            .setMessage("Are you sure you want to delete this reminder?\n\nTime: $timeDisplay\nFrequency: ${reminder.frequency}\n\nThis action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                // Cancel the scheduled alarm
                ReminderScheduler.cancelReminder(requireContext(), reminder.id)
                
                remindersViewModel.deleteReminder(reminder)
            }
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
}