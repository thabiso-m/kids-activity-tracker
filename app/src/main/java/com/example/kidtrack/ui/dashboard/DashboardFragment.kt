package com.example.kidtrack.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kidtrack.R
import com.example.kidtrack.databinding.FragmentDashboardBinding
import com.example.kidtrack.data.database.KidTrackDatabase
import com.example.kidtrack.data.repository.KidTrackRepository
import com.example.kidtrack.ui.activities.ActivitiesAdapter

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DashboardViewModel
    private lateinit var upcomingActivitiesAdapter: ActivitiesAdapter
    private lateinit var overdueTasksAdapter: ActivitiesAdapter

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

        // Observe data
        viewModel.upcomingActivities.observe(viewLifecycleOwner, Observer { activities ->
            upcomingActivitiesAdapter.submitList(activities)
        })

        viewModel.overdueTasks.observe(viewLifecycleOwner, Observer { tasks ->
            overdueTasksAdapter.submitList(tasks)
        })
        
        // Fetch dashboard data
        viewModel.loadDashboardData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}