package com.example.kidtrack.ui.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kidtrack.R
import com.example.kidtrack.data.database.KidTrackDatabase
import com.example.kidtrack.data.model.Activity
import com.example.kidtrack.data.model.Reminder
import com.example.kidtrack.data.repository.KidTrackRepository
import com.example.kidtrack.utils.DateTimeUtils
import com.example.kidtrack.utils.PredefinedTasks
import com.example.kidtrack.utils.ReminderScheduler
import com.example.kidtrack.utils.UiState
import com.example.kidtrack.utils.ValidationHelper
import com.example.kidtrack.utils.ValidationResult
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.util.Calendar

class ActivitiesFragment : Fragment(R.layout.fragment_activities) {

    private lateinit var activitiesViewModel: ActivitiesViewModel
    private lateinit var activitiesRecyclerView: RecyclerView
    private lateinit var activitiesAdapter: ActivitiesAdapter
    private lateinit var addActivityButton: ExtendedFloatingActionButton
    private lateinit var emptyStateLayout: View
    private lateinit var searchEditText: TextInputEditText
    private lateinit var filterButton: MaterialButton
    private var allActivities: List<Activity> = emptyList()
    private var currentFilter: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = KidTrackDatabase.getDatabase(requireContext())
        val repository = KidTrackRepository(database)
        val factory = ActivitiesViewModelFactory(repository)
        activitiesViewModel = ViewModelProvider(this, factory).get(ActivitiesViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        activitiesRecyclerView = view.findViewById(R.id.activitiesRecyclerView)
        activitiesRecyclerView.layoutManager = LinearLayoutManager(context)
        
        activitiesAdapter = ActivitiesAdapter(
            viewLifecycleOwner,
            onItemClick = { activity -> showEditActivityDialog(activity) },
            onItemLongClick = { activity -> showDeleteConfirmation(activity) }
        )
        activitiesRecyclerView.adapter = activitiesAdapter

        // Setup empty state
        emptyStateLayout = view.findViewById(R.id.emptyStateActivities)
        setupEmptyState()

        // Setup search
        searchEditText = view.findViewById(R.id.searchEditText)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterActivities(s.toString())
            }
        })
        
        // Setup filter button
        filterButton = view.findViewById(R.id.filterButton)
        filterButton.setOnClickListener {
            showFilterDialog()
        }

        activitiesViewModel.activities.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Show loading state
                    activitiesRecyclerView.visibility = View.GONE
                    emptyStateLayout.visibility = View.GONE
                }
                is UiState.Success -> {
                    // Hide loading and show data
                    allActivities = state.data
                    filterActivities(searchEditText.text.toString())
                    updateEmptyState(state.data.isEmpty())
                }
                is UiState.Error -> {
                    // Hide loading and show error
                    Snackbar.make(view, state.message, Snackbar.LENGTH_LONG)
                        .setAction("Retry") { activitiesViewModel.retry() }
                        .show()
                }
            }
        }
        
        // Observe operation status
        activitiesViewModel.operationStatus.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Show progress
                }
                is UiState.Success -> {
                    Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()
                    activitiesViewModel.clearOperationStatus()
                }
                is UiState.Error -> {
                    Snackbar.make(view, state.message, Snackbar.LENGTH_LONG)
                        .setAction("Dismiss") { activitiesViewModel.clearOperationStatus() }
                        .show()
                }
                else -> {}
            }
        }
        
        // Setup FAB click listener
        addActivityButton = view.findViewById(R.id.addActivityButton)
        addActivityButton.setOnClickListener {
            showAddActivityDialog()
        }
        
        // Load activities
        activitiesViewModel.fetchActivities()
    }

    private fun setupEmptyState() {
        emptyStateLayout.findViewById<ImageView>(R.id.emptyStateIcon)
            .setImageResource(android.R.drawable.ic_menu_agenda)
        emptyStateLayout.findViewById<TextView>(R.id.emptyStateTitle).text = 
            "No Activities Yet"
        emptyStateLayout.findViewById<TextView>(R.id.emptyStateMessage).text = 
            "Start tracking your child's activities by adding your first activity!"
        emptyStateLayout.findViewById<MaterialButton>(R.id.emptyStateButton).apply {
            text = "Add First Activity"
            setOnClickListener { showAddActivityDialog() }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        activitiesRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showAddActivityDialog() {
        // Fetch profiles from repository
        val database = KidTrackDatabase.getDatabase(requireContext())
        val repository = KidTrackRepository(database)
        
        viewLifecycleOwner.lifecycleScope.launch {
            val profiles = repository.getAllUserProfiles()
            
            if (profiles.isEmpty()) {
                Toast.makeText(requireContext(), "Please create a child profile first", Toast.LENGTH_LONG).show()
                return@launch
            }
            
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_activity, null)

            val profileInput = dialogView.findViewById<AutoCompleteTextView>(R.id.profileInput)
            val predefinedTaskInput = dialogView.findViewById<AutoCompleteTextView>(R.id.predefinedTaskInput)
            val categoryInput = dialogView.findViewById<TextInputEditText>(R.id.categoryInput)
            val dateInput = dialogView.findViewById<TextInputEditText>(R.id.dateInput)
            val timeInput = dialogView.findViewById<TextInputEditText>(R.id.timeInput)
            val descriptionInput = dialogView.findViewById<TextInputEditText>(R.id.descriptionInput)
            val notesInput = dialogView.findViewById<TextInputEditText>(R.id.notesInput)
            val reminderNameInput = dialogView.findViewById<TextInputEditText>(R.id.reminderNameInput)
            val enableSnoozeCheckbox = dialogView.findViewById<MaterialCheckBox>(R.id.enableSnoozeCheckbox)
            
            // Setup profile dropdown
            val profileMap = profiles.associate { "${it.name} (${it.age} years)" to it.id }
            val profileNames = profileMap.keys.toList()
            val profileAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, profileNames)
            profileInput.setAdapter(profileAdapter)

            // Setup predefined tasks dropdown
            val predefinedTasks = PredefinedTasks.ALL_TASKS
            val taskDescriptions = predefinedTasks.map { "${it.category}: ${it.description}" }
            val taskAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, taskDescriptions)
            predefinedTaskInput.setAdapter(taskAdapter)
            
            // Handle predefined task selection
            predefinedTaskInput.setOnItemClickListener { _, _, position, _ ->
                val selectedTask = predefinedTasks[position]
                categoryInput.setText(selectedTask.category)
                descriptionInput.setText(selectedTask.description)
                selectedTask.suggestedTimeMinutes?.let { minutes ->
                    timeInput.setText(DateTimeUtils.minutesToTimeString(minutes))
                }
            }

            val calendar = Calendar.getInstance()

            // Date picker
            dateInput.setOnClickListener {
                DatePickerDialog(
                    requireContext(),
                    { _, year, month, day ->
                        dateInput.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

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
                    val category = categoryInput.text.toString().trim()
                    val date = dateInput.text.toString().trim()
                    val time = timeInput.text.toString().trim()
                    val description = descriptionInput.text.toString().trim()
                    val notes = notesInput.text.toString().trim()
                    val reminderName = reminderNameInput.text.toString().trim().ifEmpty {
                        "$category Reminder"
                    }
                    val snoozeEnabled = enableSnoozeCheckbox.isChecked

                    // Validate inputs
                    val categoryValidation = ValidationHelper.combine(
                        ValidationHelper.validateNotEmpty(category, "Category"),
                        ValidationHelper.validateMaxLength(category, 50, "Category")
                    )
                    val dateValidation = ValidationHelper.validateDate(date)
                    val timeValidation = ValidationHelper.validateTime(time)
                    val descriptionValidation = ValidationHelper.combine(
                        ValidationHelper.validateNotEmpty(description, "Description"),
                        ValidationHelper.validateMaxLength(description, 200, "Description")
                    )
                    val profileValidation = if (selectedProfile.isEmpty()) {
                        ValidationResult.Error("Please select a profile")
                    } else ValidationResult.Success

                    val combinedValidation = ValidationHelper.combine(
                        categoryValidation,
                        dateValidation,
                        timeValidation,
                        descriptionValidation,
                        profileValidation
                    )

                    if (!combinedValidation.isSuccess()) {
                        Toast.makeText(requireContext(), combinedValidation.getErrorMessage(), Toast.LENGTH_LONG).show()
                        return@setPositiveButton
                    }
                    
                    val profileId = profileMap[selectedProfile] ?: 0L
                    
                    // Convert date and time to timestamp and minutes
                    val timestamp = DateTimeUtils.dateStringToTimestamp(date)
                    val timeMinutes = DateTimeUtils.timeStringToMinutes(time)
                    
                    if (timestamp == null || timeMinutes == null) {
                        Toast.makeText(requireContext(), "Invalid date or time format", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    val activity = Activity(
                        category = category,
                        dateTimestamp = timestamp,
                        timeMinutes = timeMinutes,
                        description = description,
                        notes = notes,
                        profileId = profileId
                    )
                    
                    // Add activity and always create a reminder
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            repository.insertActivity(activity)
                            val activityId = repository.getAllActivities().lastOrNull()?.id ?: 0L
                            
                            if (activityId > 0) {
                                // Always create reminder 1 day before at 9:00 AM
                                val reminder = Reminder(
                                    name = reminderName,
                                    timeMinutes = 540, // 9:00 AM
                                    frequency = "once",
                                    associatedActivityId = activityId,
                                    profileId = profileId,
                                    daysBefore = 1,
                                    eventDateTimestamp = timestamp,
                                    snoozeEnabled = snoozeEnabled
                                )
                                repository.insertReminder(reminder)
                                
                                // Schedule the reminder
                                val reminderId = repository.getAllReminders().lastOrNull()?.id ?: 0L
                                if (reminderId > 0) {
                                    val scheduledReminder = reminder.copy(id = reminderId)
                                    ReminderScheduler.scheduleReminder(requireContext(), scheduledReminder)
                                }
                            }
                            
                            activitiesViewModel.fetchActivities()
                            Toast.makeText(requireContext(), "Activity and reminder added successfully", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Failed to add activity: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showEditActivityDialog(activity: Activity) {
        val database = KidTrackDatabase.getDatabase(requireContext())
        val repository = KidTrackRepository(database)
        
        viewLifecycleOwner.lifecycleScope.launch {
            val profiles = repository.getAllUserProfiles()
            
            if (profiles.isEmpty()) {
                Toast.makeText(requireContext(), "No profiles available", Toast.LENGTH_LONG).show()
                return@launch
            }
            
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_activity, null)

            val profileInput = dialogView.findViewById<AutoCompleteTextView>(R.id.profileInput)
            val categoryInput = dialogView.findViewById<TextInputEditText>(R.id.categoryInput)
            val dateInput = dialogView.findViewById<TextInputEditText>(R.id.dateInput)
            val timeInput = dialogView.findViewById<TextInputEditText>(R.id.timeInput)
            val descriptionInput = dialogView.findViewById<TextInputEditText>(R.id.descriptionInput)
            val notesInput = dialogView.findViewById<TextInputEditText>(R.id.notesInput)
            val reminderNameInput = dialogView.findViewById<TextInputEditText>(R.id.reminderNameInput)
            val enableSnoozeCheckbox = dialogView.findViewById<MaterialCheckBox>(R.id.enableSnoozeCheckbox)

            // Pre-fill with existing data
            categoryInput.setText(activity.category)
            dateInput.setText(DateTimeUtils.timestampToDateString(activity.dateTimestamp))
            timeInput.setText(DateTimeUtils.minutesToTimeString(activity.timeMinutes))
            descriptionInput.setText(activity.description)
            notesInput.setText(activity.notes)
            
            // Load existing reminder info
            val existingReminder = repository.getAllReminders().find { it.associatedActivityId == activity.id }
            if (existingReminder != null) {
                reminderNameInput.setText(existingReminder.name)
                enableSnoozeCheckbox.isChecked = existingReminder.snoozeEnabled
            }
            
            // Setup profile dropdown
            val profileMap = profiles.associate { "${it.name} (${it.age} years)" to it.id }
            val profileNames = profileMap.keys.toList()
            val profileAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, profileNames)
            profileInput.setAdapter(profileAdapter)
            
            // Pre-select current profile
            val currentProfile = profiles.find { it.id == activity.profileId }
            if (currentProfile != null) {
                profileInput.setText("${currentProfile.name} (${currentProfile.age} years)", false)
            }

            val calendar = Calendar.getInstance()

            // Date picker
            dateInput.setOnClickListener {
                DatePickerDialog(
                    requireContext(),
                    { _, year, month, day ->
                        dateInput.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

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
                .setTitle("Edit Activity")
                .setView(dialogView)
                .setPositiveButton("Update") { _, _ ->
                    val selectedProfile = profileInput.text.toString().trim()
                    val category = categoryInput.text.toString().trim()
                    val date = dateInput.text.toString().trim()
                    val time = timeInput.text.toString().trim()
                    val description = descriptionInput.text.toString().trim()
                    val notes = notesInput.text.toString().trim()
                    val reminderName = reminderNameInput.text.toString().trim().ifEmpty {
                        "$category Reminder"
                    }
                    val snoozeEnabled = enableSnoozeCheckbox.isChecked

                    // Validate inputs
                    val categoryValidation = ValidationHelper.combine(
                        ValidationHelper.validateNotEmpty(category, "Category"),
                        ValidationHelper.validateMaxLength(category, 50, "Category")
                    )
                    val dateValidation = ValidationHelper.validateDate(date)
                    val timeValidation = ValidationHelper.validateTime(time)
                    val descriptionValidation = ValidationHelper.combine(
                        ValidationHelper.validateNotEmpty(description, "Description"),
                        ValidationHelper.validateMaxLength(description, 200, "Description")
                    )
                    val profileValidation = if (selectedProfile.isEmpty()) {
                        ValidationResult.Error("Please select a profile")
                    } else ValidationResult.Success

                    val combinedValidation = ValidationHelper.combine(
                        categoryValidation,
                        dateValidation,
                        timeValidation,
                        descriptionValidation,
                        profileValidation
                    )

                    if (!combinedValidation.isSuccess()) {
                        Toast.makeText(requireContext(), combinedValidation.getErrorMessage(), Toast.LENGTH_LONG).show()
                        return@setPositiveButton
                    }
                    
                    val profileId = profileMap[selectedProfile] ?: activity.profileId
                    
                    // Convert date and time to timestamp and minutes
                    val timestamp = DateTimeUtils.dateStringToTimestamp(date)
                    val timeMinutes = DateTimeUtils.timeStringToMinutes(time)
                    
                    if (timestamp == null || timeMinutes == null) {
                        Toast.makeText(requireContext(), "Invalid date or time format", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    val updatedActivity = activity.copy(
                        category = category,
                        dateTimestamp = timestamp,
                        timeMinutes = timeMinutes,
                        description = description,
                        notes = notes,
                        profileId = profileId
                    )
                    
                    // Update activity and its reminder
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            activitiesViewModel.updateActivity(updatedActivity)
                            
                            // Update the associated reminder
                            val reminder = repository.getAllReminders().find { it.associatedActivityId == activity.id }
                            if (reminder != null) {
                                val updatedReminder = reminder.copy(
                                    name = reminderName,
                                    profileId = profileId,
                                    eventDateTimestamp = timestamp,
                                    snoozeEnabled = snoozeEnabled
                                )
                                repository.insertReminder(updatedReminder)
                                
                                // Reschedule the reminder
                                ReminderScheduler.cancelReminder(requireContext(), reminder.id)
                                ReminderScheduler.scheduleReminder(requireContext(), updatedReminder)
                            }
                            
                            Toast.makeText(requireContext(), "Activity and reminder updated", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Failed to update: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showDeleteConfirmation(activity: Activity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Activity")
            .setMessage("Are you sure you want to delete \"${activity.category}\"?\n\nThis action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                activitiesViewModel.deleteActivity(activity)
                Toast.makeText(requireContext(), "Activity deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
    
    private fun filterActivities(query: String) {
        var filtered = allActivities
        
        // Apply search filter
        if (query.isNotBlank()) {
            filtered = filtered.filter { activity ->
                activity.description.contains(query, ignoreCase = true) ||
                activity.category.contains(query, ignoreCase = true) ||
                activity.notes.contains(query, ignoreCase = true)
            }
        }
        
        // Apply category filter
        currentFilter?.let { filter ->
            filtered = filtered.filter { it.category == filter }
        }
        
        activitiesAdapter.submitList(filtered)
    }
    
    private fun showFilterDialog() {
        val categories = allActivities.map { it.category }.distinct().toTypedArray()
        
        if (categories.isEmpty()) {
            Toast.makeText(requireContext(), "No categories available", Toast.LENGTH_SHORT).show()
            return
        }
        
        val options = arrayOf("All Categories") + categories
        var selectedIndex = if (currentFilter == null) 0 else options.indexOf(currentFilter) 
        
        AlertDialog.Builder(requireContext())
            .setTitle("Filter by Category")
            .setSingleChoiceItems(options, selectedIndex) { _, which ->
                selectedIndex = which
            }
            .setPositiveButton("Apply") { _, _ ->
                currentFilter = if (selectedIndex == 0) null else options[selectedIndex]
                filterActivities(searchEditText.text.toString())
                filterButton.text = if (currentFilter == null) "Filter" else "Filter: $currentFilter"
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}