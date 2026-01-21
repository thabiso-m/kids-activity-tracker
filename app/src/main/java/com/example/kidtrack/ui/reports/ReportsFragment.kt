package com.example.kidtrack.ui.reports

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kidtrack.R
import com.example.kidtrack.data.database.KidTrackDatabase
import com.example.kidtrack.data.repository.KidTrackRepository
import com.example.kidtrack.ui.activities.ActivitiesAdapter
import com.example.kidtrack.utils.ReportExporter
import com.example.kidtrack.utils.UiState
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class ReportsFragment : Fragment() {

    private lateinit var reportsViewModel: ReportsViewModel
    private lateinit var activitiesAdapter: ActivitiesAdapter
    private lateinit var generateReportButton: ExtendedFloatingActionButton
    private lateinit var reportsRecyclerView: RecyclerView
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart

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

        // Initialize charts
        pieChart = view.findViewById(R.id.pieChart)
        barChart = view.findViewById(R.id.barChart)
        setupCharts()

        // Setup FAB
        generateReportButton = view.findViewById(R.id.generate_report_button)
        generateReportButton.setOnClickListener {
            showReportDialog()
        }

        // Observe statistics with UiState
        reportsViewModel.statistics.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    hideCharts()
                }
                is UiState.Success -> {
                    showCharts()
                    updateStatistics(state.data)
                }
                is UiState.Error -> {
                    hideCharts()
                    Snackbar.make(view, state.message, Snackbar.LENGTH_LONG)
                        .setAction("Retry") { reportsViewModel.retry() }
                        .show()
                }
            }
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
        
        // Update charts
        updatePieChart(stats.categoryBreakdown)
        updateBarChart(stats)
    }
    
    private fun setupCharts() {
        // Setup Pie Chart
        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.setDrawHoleEnabled(true)
        pieChart.setHoleColor(android.graphics.Color.WHITE)
        pieChart.setTransparentCircleColor(android.graphics.Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f
        pieChart.setDrawCenterText(true)
        pieChart.centerText = "Categories"
        pieChart.setCenterTextSize(14f)
        pieChart.setRotationAngle(0f)
        pieChart.setRotationEnabled(true)
        pieChart.setHighlightPerTapEnabled(true)
        pieChart.animateY(1400)
        
        // Setup Bar Chart
        barChart.description.isEnabled = false
        barChart.setFitBars(true)
        barChart.animateY(1000)
        barChart.legend.isEnabled = true
        barChart.xAxis.setDrawGridLines(false)
        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisRight.isEnabled = false
    }
    
    private fun showCharts() {
        pieChart.visibility = View.VISIBLE
        barChart.visibility = View.VISIBLE
        reportsRecyclerView.visibility = View.VISIBLE
    }
    
    private fun hideCharts() {
        pieChart.visibility = View.GONE
        barChart.visibility = View.GONE
        reportsRecyclerView.visibility = View.GONE
    }
    
    private fun updatePieChart(categoryBreakdown: Map<String, Int>) {
        if (categoryBreakdown.isEmpty()) {
            pieChart.clear()
            pieChart.setNoDataText("No activity data available")
            return
        }
        
        val entries = ArrayList<PieEntry>()
        for ((category, count) in categoryBreakdown) {
            entries.add(PieEntry(count.toFloat(), category))
        }
        
        val dataSet = PieDataSet(entries, "")
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        
        val colors = ArrayList<Int>()
        colors.add(android.graphics.Color.parseColor("#0061A4"))
        colors.add(android.graphics.Color.parseColor("#6B5778"))
        colors.add(android.graphics.Color.parseColor("#855300"))
        colors.add(android.graphics.Color.parseColor("#4CAF50"))
        colors.add(android.graphics.Color.parseColor("#FF9500"))
        dataSet.colors = colors
        
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = android.graphics.Color.WHITE
        
        val data = PieData(dataSet)
        data.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${value.toInt()}"
            }
        })
        
        pieChart.data = data
        pieChart.invalidate()
    }
    
    private fun updateBarChart(stats: com.example.kidtrack.data.model.ReportStatistics) {
        val entries = ArrayList<BarEntry>()
        
        // Sample data for last 7 days
        val calendar = Calendar.getInstance()
        for (i in 6 downTo 0) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            // In real implementation, get actual count from database
            val count = if (i < 3) stats.thisWeekActivities / 7 else (stats.thisWeekActivities / 7) + 1
            entries.add(BarEntry(i.toFloat(), count.toFloat()))
        }
        
        val dataSet = BarDataSet(entries, "Activities per Day")
        dataSet.color = android.graphics.Color.parseColor("#0061A4")
        dataSet.valueTextColor = android.graphics.Color.BLACK
        dataSet.valueTextSize = 10f
        
        val data = BarData(dataSet)
        data.barWidth = 0.9f
        
        barChart.data = data
        barChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -value.toInt())
                return SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time)
            }
        }
        barChart.invalidate()
    }

    private fun showReportDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_report, null)

        val statsState = reportsViewModel.statistics.value
        
        // Extract stats from UiState
        val stats = when (statsState) {
            is UiState.Success -> statsState.data
            else -> null
        }

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
            .setNeutralButton("Share PDF") { _, _ ->
                exportAndSharePdf()
            }
            .setNegativeButton("Share Image") { _, _ ->
                exportAndShareImage(dialogView)
            }
            .show()
    }

    private fun exportAndSharePdf() {
        val statsState = reportsViewModel.statistics.value
        val stats = when (statsState) {
            is UiState.Success -> statsState.data
            else -> null
        }
        
        if (stats != null) {
            val file = ReportExporter.exportReportAsPdf(
                requireContext(),
                stats,
                stats.recentActivities
            )
            if (file != null) {
                ReportExporter.shareFile(requireContext(), file, "application/pdf")
            } else {
                Toast.makeText(requireContext(), "Failed to generate PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exportAndShareImage(view: View) {
        val file = ReportExporter.exportViewAsImage(requireContext(), view)
        if (file != null) {
            ReportExporter.shareFile(requireContext(), file, "image/png")
        } else {
            Toast.makeText(requireContext(), "Failed to generate image", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh statistics when returning to fragment
        reportsViewModel.loadStatistics()
    }
}