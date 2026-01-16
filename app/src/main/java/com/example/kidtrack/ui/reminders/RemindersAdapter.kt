package com.example.kidtrack.ui.reminders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kidtrack.R
import com.example.kidtrack.data.model.Reminder
import com.example.kidtrack.utils.DateTimeUtils

class RemindersAdapter(
    private val onItemClick: (Reminder) -> Unit = {},
    private val onItemLongClick: (Reminder) -> Unit = {}
) : ListAdapter<Reminder, RemindersAdapter.ReminderViewHolder>(ReminderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view, onItemClick, onItemLongClick)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReminderViewHolder(
        itemView: View,
        private val onItemClick: (Reminder) -> Unit,
        private val onItemLongClick: (Reminder) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.reminderTitle)
        private val timeTextView: TextView = itemView.findViewById(R.id.reminderTime)
        private val messageTextView: TextView = itemView.findViewById(R.id.reminderMessage)

        fun bind(reminder: Reminder) {
            titleTextView.text = "Reminder #${reminder.id}"
            timeTextView.text = DateTimeUtils.minutesToTimeString(reminder.timeMinutes)
            messageTextView.text = "Frequency: ${reminder.frequency}"
            
            itemView.setOnClickListener {
                onItemClick(reminder)
            }
            
            itemView.setOnLongClickListener {
                onItemLongClick(reminder)
                true
            }
        }
    }

    class ReminderDiffCallback : DiffUtil.ItemCallback<Reminder>() {
        override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
            return oldItem == newItem
        }
    }
}
