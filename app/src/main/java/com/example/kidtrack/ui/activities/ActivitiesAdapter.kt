package com.example.kidtrack.ui.activities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kidtrack.R
import com.example.kidtrack.data.database.KidTrackDatabase
import com.example.kidtrack.data.model.Activity
import com.example.kidtrack.data.repository.KidTrackRepository
import com.example.kidtrack.utils.DateTimeUtils
import kotlinx.coroutines.launch

class ActivitiesAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val onItemClick: (Activity) -> Unit = {},
    private val onItemLongClick: (Activity) -> Unit = {}
) : ListAdapter<Activity, ActivitiesAdapter.ActivityViewHolder>(ActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ActivityViewHolder(view, lifecycleOwner, onItemClick, onItemLongClick)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ActivityViewHolder(
        itemView: View,
        private val lifecycleOwner: LifecycleOwner,
        private val onItemClick: (Activity) -> Unit,
        private val onItemLongClick: (Activity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.activityTitle)
        private val profileNameTextView: TextView = itemView.findViewById(R.id.activityProfileName)
        private val dateTextView: TextView = itemView.findViewById(R.id.activityDate)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.activityDescription)
        private val reminderInfoTextView: TextView = itemView.findViewById(R.id.activityReminderInfo)

        fun bind(activity: Activity) {
            titleTextView.text = activity.category
            
            // Format timestamp and time for display
            val dateString = DateTimeUtils.timestampToDateString(activity.dateTimestamp)
            val timeString = DateTimeUtils.minutesToTimeString(activity.timeMinutes)
            dateTextView.text = "$dateString at $timeString"
            
            descriptionTextView.text = activity.description
            
            val database = KidTrackDatabase.getDatabase(itemView.context)
            val repository = KidTrackRepository(database)
            
            lifecycleOwner.lifecycleScope.launch {
                // Load and display profile name
                if (activity.profileId != 0L) {
                    val profile = repository.getAllUserProfiles().find { it.id == activity.profileId }
                    if (profile != null) {
                        profileNameTextView.text = "👤 ${profile.name}"
                        profileNameTextView.visibility = View.VISIBLE
                    } else {
                        profileNameTextView.visibility = View.GONE
                    }
                } else {
                    profileNameTextView.visibility = View.GONE
                }
                
                // Load and display reminder info
                val reminder = repository.getAllReminders().find { it.associatedActivityId == activity.id }
                if (reminder != null) {
                    val snoozeStatus = if (reminder.snoozeEnabled) "On" else "Off"
                    reminderInfoTextView.text = "Reminder: ${reminder.name} • Snooze: $snoozeStatus"
                    reminderInfoTextView.visibility = View.VISIBLE
                } else {
                    reminderInfoTextView.visibility = View.GONE
                }
            }
            
            itemView.setOnClickListener {
                onItemClick(activity)
            }
            
            itemView.setOnLongClickListener {
                onItemLongClick(activity)
                true
            }
        }
    }

    class ActivityDiffCallback : DiffUtil.ItemCallback<Activity>() {
        override fun areItemsTheSame(oldItem: Activity, newItem: Activity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Activity, newItem: Activity): Boolean {
            return oldItem == newItem
        }
    }
}
