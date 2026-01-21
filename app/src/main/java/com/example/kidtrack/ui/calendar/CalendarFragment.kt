package com.example.kidtrack.ui.calendar

import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kidtrack.R
import com.example.kidtrack.data.database.KidTrackDatabase
import com.example.kidtrack.data.repository.KidTrackRepository
import com.example.kidtrack.ui.activities.ActivitiesAdapter
import com.example.kidtrack.ui.activities.ActivitiesViewModel
import com.example.kidtrack.ui.activities.ActivitiesViewModelFactory
import com.example.kidtrack.utils.DateTimeUtils
import com.example.kidtrack.utils.UiState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment(R.layout.fragment_calendar) {

    private lateinit var calendarView: CalendarView
    private lateinit var selectedDateText: TextView
    private lateinit var activitiesRecyclerView: RecyclerView
    private lateinit var noActivitiesText: TextView
    private lateinit var activitiesViewModel: ActivitiesViewModel
    private lateinit var activitiesAdapter: ActivitiesAdapter
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = KidTrackDatabase.getDatabase(requireContext())
        val repository = KidTrackRepository(database)
        val factory = ActivitiesViewModelFactory(repository)
        activitiesViewModel = ViewModelProvider(this, factory).get(ActivitiesViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarView = view.findViewById(R.id.calendarView)
        selectedDateText = view.findViewById(R.id.selectedDateText)
        activitiesRecyclerView = view.findViewById(R.id.activitiesForDateRecyclerView)
        noActivitiesText = view.findViewById(R.id.noActivitiesText)

        // Setup RecyclerView
        activitiesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        activitiesAdapter = ActivitiesAdapter(viewLifecycleOwner, onItemClick = {}, onItemLongClick = {})
        activitiesRecyclerView.adapter = activitiesAdapter

        // Setup calendar listener
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            updateActivitiesForDate(calendar.time)
        }

        // Observe activities
        activitiesViewModel.activities.observe(viewLifecycleOwner) { _ ->
            // Update current selected date
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = calendarView.date
            updateActivitiesForDate(calendar.time)
        }

        // Load initial data for today
        updateActivitiesForDate(Date())
        activitiesViewModel.fetchActivities()
    }

    private fun updateActivitiesForDate(date: Date) {
        val dateString = dateFormat.format(date)
        selectedDateText.text = displayDateFormat.format(date)

        lifecycleScope.launch {
            val timestamp = DateTimeUtils.dateStringToTimestamp(dateString)
            val activitiesState = activitiesViewModel.activities.value
            val activities = when (activitiesState) {
                is UiState.Success -> activitiesState.data.filter { it.dateTimestamp == timestamp }
                else -> emptyList()
            }
            
            if (activities.isEmpty()) {
                activitiesRecyclerView.visibility = View.GONE
                noActivitiesText.visibility = View.VISIBLE
            } else {
                activitiesRecyclerView.visibility = View.VISIBLE
                noActivitiesText.visibility = View.GONE
                activitiesAdapter.submitList(activities)
            }
        }
    }
}
