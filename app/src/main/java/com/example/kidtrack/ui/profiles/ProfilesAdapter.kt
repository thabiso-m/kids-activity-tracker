package com.example.kidtrack.ui.profiles

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kidtrack.R
import com.example.kidtrack.data.model.UserProfile

class ProfilesAdapter(
    private val onItemClick: (UserProfile) -> Unit = {},
    private val onItemLongClick: (UserProfile) -> Unit = {}
) : ListAdapter<UserProfile, ProfilesAdapter.ProfileViewHolder>(ProfileDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile, parent, false)
        return ProfileViewHolder(view, onItemClick, onItemLongClick)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ProfileViewHolder(
        itemView: View,
        private val onItemClick: (UserProfile) -> Unit,
        private val onItemLongClick: (UserProfile) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val photoImageView: ImageView = itemView.findViewById(R.id.profilePhoto)
        private val nameTextView: TextView = itemView.findViewById(R.id.profileName)
        private val ageTextView: TextView = itemView.findViewById(R.id.profileAge)

        fun bind(profile: UserProfile) {
            nameTextView.text = profile.name
            ageTextView.text = "${profile.age} years old"
            
            // Load profile photo
            if (profile.photoUrl != null) {
                try {
                    photoImageView.setImageURI(Uri.parse(profile.photoUrl))
                    photoImageView.imageTintList = null // Remove tint for actual photos
                } catch (e: Exception) {
                    photoImageView.setImageResource(android.R.drawable.ic_menu_myplaces)
                }
            } else {
                photoImageView.setImageResource(android.R.drawable.ic_menu_myplaces)
            }
            
            itemView.setOnClickListener {
                onItemClick(profile)
            }
            
            itemView.setOnLongClickListener {
                onItemLongClick(profile)
                true
            }
        }
    }

    class ProfileDiffCallback : DiffUtil.ItemCallback<UserProfile>() {
        override fun areItemsTheSame(oldItem: UserProfile, newItem: UserProfile): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UserProfile, newItem: UserProfile): Boolean {
            return oldItem == newItem
        }
    }
}
