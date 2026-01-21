package com.example.kidtrack.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kidtrack.R
import com.example.kidtrack.databinding.FragmentDashboardBinding
import com.example.kidtrack.data.database.KidTrackDatabase
import com.example.kidtrack.data.repository.KidTrackRepository
import com.example.kidtrack.ui.activities.ActivitiesAdapter
import com.example.kidtrack.utils.UiState
import com.google.android.material.snackbar.Snackbar

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DashboardViewModel
    private lateinit var upcomingActivitiesAdapter: ActivitiesAdapter
    private lateinit var overdueTasksAdapter: ActivitiesAdapter

    private val PREFS_NAME = "KidTrackPrefs"
    private val WELCOME_DISMISSED_KEY = "welcome_card_dismissed"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val database = KidTrackDatabase.getDatabase(requireContext())
        val repository = KidTrackRepository(database)
        val factory = DashboardViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(DashboardViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWelcomeCard()
        setupRecyclerViews(view)
        observeData(view)

        // Fetch dashboard data
        viewModel.loadDashboardData()
    }

    private fun setupWelcomeCard() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isWelcomeDismissed = prefs.getBoolean(WELCOME_DISMISSED_KEY, false)

        if (isWelcomeDismissed) {
            binding.welcomeCard.visibility = View.GONE
        }

        binding.btnDismissWelcome.setOnClickListener {
            binding.welcomeCard.visibility = View.GONE
            prefs.edit().putBoolean(WELCOME_DISMISSED_KEY, true).apply()
        }

        binding.btnGetStarted.setOnClickListener {
            // Navigate to Activities fragment
            findNavController().navigate(R.id.activitiesFragment)
        }
    }

    private fun setupRecyclerViews(view: View) {
        // Initialize upcoming activities RecyclerView
        upcomingActivitiesAdapter = ActivitiesAdapter(
            viewLifecycleOwner,
            onItemClick = { activity ->
                // Handle upcoming activity click
            })
        val upcomingRecyclerView: RecyclerView = view.findViewById(R.id.recyclerViewActivities)
        upcomingRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        upcomingRecyclerView.adapter = upcomingActivitiesAdapter

        // Initialize overdue tasks RecyclerView
        overdueTasksAdapter = ActivitiesAdapter(
            viewLifecycleOwner,
            onItemClick = { task ->
                // Handle overdue task click
            })
        val overdueRecyclerView: RecyclerView = view.findViewById(R.id.recyclerViewOverdue)
        overdueRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        overdueRecyclerView.adapter = overdueTasksAdapter
    }

    private fun observeData(view: View) {
        val upcomingRecyclerView: RecyclerView = view.findViewById(R.id.recyclerViewActivities)
        val overdueRecyclerView: RecyclerView = view.findViewById(R.id.recyclerViewOverdue)

        // Observe upcoming activities with UiState
        viewModel.upcomingActivities.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    upcomingRecyclerView.visibility = View.GONE
                    binding.tvNoActivities.visibility = View.GONE
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        upcomingRecyclerView.visibility = View.GONE
                        binding.tvNoActivities.visibility = View.VISIBLE
                        // Show welcome card if no activities
                        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        val isWelcomeDismissed = prefs.getBoolean(WELCOME_DISMISSED_KEY, false)
                        if (!isWelcomeDismissed) {
                            binding.welcomeCard.visibility = View.VISIBLE
                        }
                    } else {
                        upcomingRecyclerView.visibility = View.VISIBLE
                        binding.tvNoActivities.visibility = View.GONE
                        upcomingActivitiesAdapter.submitList(state.data)
                    }
                }
                is UiState.Error -> {
                    Snackbar.make(view, state.message, Snackbar.LENGTH_LONG)
                        .setAction("Retry") { viewModel.retry() }
                        .show()
                }
            }
        }

        // Observe overdue tasks with UiState
        viewModel.overdueTasks.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    overdueRecyclerView.visibility = View.GONE
                    binding.tvNoOverdue.visibility = View.GONE
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        overdueRecyclerView.visibility = View.GONE
                        binding.tvNoOverdue.visibility = View.VISIBLE
                    } else {
                        overdueRecyclerView.visibility = View.VISIBLE
                        binding.tvNoOverdue.visibility = View.GONE
                        overdueTasksAdapter.submitList(state.data)
                    }
                }
                is UiState.Error -> {
                    Snackbar.make(view, state.message, Snackbar.LENGTH_LONG)
                        .setAction("Retry") { viewModel.retry() }
                        .show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
