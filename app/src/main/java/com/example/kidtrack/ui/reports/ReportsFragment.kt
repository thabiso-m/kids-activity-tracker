package com.example.kidtrack.ui.reports

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kidtrack.R
import com.example.kidtrack.data.database.KidTrackDatabase
import com.example.kidtrack.data.repository.KidTrackRepository
import com.example.kidtrack.ui.activities.ActivitiesAdapter
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class ReportsFragment : Fragment() {

    private lateinit var reportsViewModel: ReportsViewModel
    private lateinit var activitiesAdapter: ActivitiesAdapter
    private lateinit var generateReportButton: ExtendedFloatingActionButton
    private lateinit var reportsRecyclerView: RecyclerView

    // Stat card views
    private lateinit var statTotalValue: TextView
    private lateinit var statCompletedValue: TextView
    private lateinit var statWeekValue: TextView
    private lateinit var statRateValue: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val database = KidTrackDatabase.getDatabase(requireContext())
        val repository = KidTrackRepository(database)
        val factory = ReportsViewModelFactory(repository)
        reportsViewModel = ViewModelProvider(this, factory).get(ReportsViewModel::class.java)
        
        return inflater.inflate(R.layout.fragment_reports, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize stat card views
        val statCardTotal = view.findViewById<View>(R.id.statCardTotal)
        statTotalValue = statCardTotal.findViewById(R.id.statValue)
        statCardTotal.findViewById<TextView>(R.id.statTitle).text = "Total Activities"
        statCardTotal.findViewById<TextView>(R.id.statSubtitle).text = "All time"

        val statCardCompleted = view.findViewById<View>(R.id.statCardCompleted)
        statCompletedValue = statCardCompleted.findViewById(R.id.statValue)
        statCardCompleted.findViewById<TextView>(R.id.statTitle).text = "Completed"
        statCardCompleted.findViewById<TextView>(R.id.statSubtitle).text = "All time"

        val statCardWeek = view.findViewById<View>(R.id.statCardThisWeek)
        statWeekValue = statCardWeek.findViewById(R.id.statValue)
        statCardWeek.findViewById<TextView>(R.id.statTitle).text = "This Week"
        statCardWeek.findViewById<TextView>(R.id.statSubtitle).text = "7 days"

        val statCardRate = view.findViewById<View>(R.id.statCardRate)
        statRateValue = statCardRate.findViewById(R.id.statValue)
        statCardRate.findViewById<TextView>(R.id.statTitle).text = "Success Rate"
        statCardRate.findViewById<TextView>(R.id.statSubtitle).text = "Completion %"

        // Initialize RecyclerView
        reportsRecyclerView = view.findViewById(R.id.reports_recycler_view)
        reportsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        activitiesAdapter = ActivitiesAdapter(
            viewLifecycleOwner,
            onItemClick = { activity ->
            // Handle activity click
        })
        reportsRecyclerView.adapter = activitiesAdapter

        // Setup FAB
        generateReportButton = view.findViewById(R.id.generate_report_button)
        generateReportButton.setOnClickListener {
            showReportDialog()
        }

        // Observe statistics
        reportsViewModel.statistics.observe(viewLifecycleOwner) { stats ->
            updateStatistics(stats)
        }

        // Load initial data
        reportsViewModel.loadStatistics()
    }

    private fun updateStatistics(stats: com.example.kidtrack.data.model.ReportStatistics) {
        statTotalValue.text = stats.totalActivities.toString()
        statCompletedValue.text = stats.completedActivities.toString()
        statWeekValue.text = stats.thisWeekActivities.toString()
        statRateValue.text = "${stats.completionRate}%"

        // Update recent activities list
        activitiesAdapter.submitList(stats.recentActivities)
    }

    private fun showReportDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_report, null)

        val stats = reportsViewModel.statistics.value

        if (stats != null) {
            // Set report period
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            dialogView.findViewById<TextView>(R.id.reportPeriod).text =
                "Week of ${dateFormat.format(calendar.time)}"

            // Set statistics
            dialogView.findViewById<TextView>(R.id.reportTotalActivities).text =
                stats.totalActivities.toString()
            dialogView.findViewById<TextView>(R.id.reportCompletedActivities).text =
                stats.completedActivities.toString()
            dialogView.findViewById<TextView>(R.id.reportCompletionRate).text =
                "${stats.completionRate}%"

            // Set categories
            val categoriesText = if (stats.categoryBreakdown.isEmpty()) {
                "No categories available"
            } else {
                stats.categoryBreakdown.entries.joinToString("\n") { (category, count) ->
                    "• $category: $count activities"
                }
            }
            dialogView.findViewById<TextView>(R.id.reportCategories).text = categoriesText

            // Set insights
            val insights = buildString {
                if (stats.totalActivities > 0) {
                    append("Great job! You've logged ${stats.totalActivities} activities.\n")
                    if (stats.completionRate >= 70) {
                        append("Your completion rate of ${stats.completionRate}% is excellent!\n")
                    } else {
                        append("Keep going! Try to increase your completion rate.\n")
                    }
                    if (stats.thisWeekActivities > 0) {
                        append("This week you've added ${stats.thisWeekActivities} activities.")
                    }
                } else {
                    append("Start adding activities to track progress!")
                }
            }
            dialogView.findViewById<TextView>(R.id.reportInsights).text = insights
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Refresh statistics when returning to fragment
        reportsViewModel.loadStatistics()
    }
}