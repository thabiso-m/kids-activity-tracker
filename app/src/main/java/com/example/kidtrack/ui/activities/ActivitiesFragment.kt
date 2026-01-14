package com.example.kidtrack.ui.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
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
import com.example.kidtrack.data.repository.KidTrackRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.util.Calendar

class ActivitiesFragment : Fragment(R.layout.fragment_activities) {

    private lateinit var activitiesViewModel: ActivitiesViewModel
    private lateinit var activitiesRecyclerView: RecyclerView
    private lateinit var activitiesAdapter: ActivitiesAdapter
    private lateinit var addActivityButton: ExtendedFloatingActionButton
    private lateinit var emptyStateLayout: View

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

        activitiesViewModel.activities.observe(viewLifecycleOwner) { activities ->
            activitiesAdapter.submitList(activities)
            updateEmptyState(activities.isEmpty())
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
            val categoryInput = dialogView.findViewById<TextInputEditText>(R.id.categoryInput)
            val dateInput = dialogView.findViewById<TextInputEditText>(R.id.dateInput)
            val timeInput = dialogView.findViewById<TextInputEditText>(R.id.timeInput)
            val descriptionInput = dialogView.findViewById<TextInputEditText>(R.id.descriptionInput)
            val notesInput = dialogView.findViewById<TextInputEditText>(R.id.notesInput)
            
            // Setup profile dropdown
            val profileMap = profiles.associate { "${it.name} (${it.age} years)" to it.id }
            val profileNames = profileMap.keys.toList()
            val profileAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, profileNames)
            profileInput.setAdapter(profileAdapter)

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

                    if (selectedProfile.isEmpty() || category.isEmpty() || date.isEmpty() || time.isEmpty() || description.isEmpty()) {
                        Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    
                    val profileId = profileMap[selectedProfile] ?: 0L

                    val activity = Activity(
                        category = category,
                        date = date,
                        time = time,
                        description = description,
                        notes = notes,
                        profileId = profileId
                    )
                    activitiesViewModel.addActivity(activity)
                    Toast.makeText(requireContext(), "Activity added successfully", Toast.LENGTH_SHORT).show()
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

            // Pre-fill with existing data
            categoryInput.setText(activity.category)
            dateInput.setText(activity.date)
            timeInput.setText(activity.time)
            descriptionInput.setText(activity.description)
            notesInput.setText(activity.notes)
            
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

                    if (selectedProfile.isEmpty() || category.isEmpty() || date.isEmpty() || time.isEmpty() || description.isEmpty()) {
                        Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    
                    val profileId = profileMap[selectedProfile] ?: activity.profileId

                    val updatedActivity = activity.copy(
                        category = category,
                        date = date,
                        time = time,
                        description = description,
                        notes = notes,
                        profileId = profileId
                    )
                    activitiesViewModel.updateActivity(updatedActivity)
                    Toast.makeText(requireContext(), "Activity updated successfully", Toast.LENGTH_SHORT).show()
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
}