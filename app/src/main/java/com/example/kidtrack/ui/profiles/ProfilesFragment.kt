package com.example.kidtrack.ui.profiles

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kidtrack.R
import com.example.kidtrack.data.database.KidTrackDatabase
import com.example.kidtrack.data.model.UserProfile
import com.example.kidtrack.data.repository.KidTrackRepository
import com.example.kidtrack.utils.UiState
import com.example.kidtrack.utils.ValidationHelper
import com.example.kidtrack.utils.ValidationResult
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class ProfilesFragment : Fragment() {

    private lateinit var viewModel: ProfilesViewModel
    private lateinit var profilesAdapter: ProfilesAdapter
    private lateinit var profilesRecyclerView: RecyclerView
    private lateinit var addProfileButton: ExtendedFloatingActionButton
    private lateinit var emptyStateLayout: View
    private var selectedImageUri: Uri? = null
    private var currentPhotoPreview: ImageView? = null
    
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            currentPhotoPreview?.setImageURI(it)
            // Take persistable permission to access the image
            requireContext().contentResolver.takePersistableUriPermission(
                it,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val database = KidTrackDatabase.getDatabase(requireContext())
        val repository = KidTrackRepository(database)
        val factory = ProfilesViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(ProfilesViewModel::class.java)
        
        return inflater.inflate(R.layout.fragment_profiles, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize RecyclerView
        profilesRecyclerView = view.findViewById(R.id.profiles_recycler_view)
        profilesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        // Initialize adapter with click listener
        profilesAdapter = ProfilesAdapter(
            onItemClick = { profile -> showEditProfileDialog(profile) },
            onItemLongClick = { profile -> showDeleteConfirmation(profile) }
        )
        profilesRecyclerView.adapter = profilesAdapter
        
        // Setup empty state
        emptyStateLayout = view.findViewById(R.id.emptyStateProfiles)
        setupEmptyState()
        
        // Observe profiles data with UiState
        viewModel.profiles.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    profilesRecyclerView.visibility = View.GONE
                    emptyStateLayout.visibility = View.GONE
                }
                is UiState.Success -> {
                    profilesAdapter.submitList(state.data)
                    updateEmptyState(state.data.isEmpty())
                }
                is UiState.Error -> {
                    Snackbar.make(view, state.message, Snackbar.LENGTH_LONG)
                        .setAction("Retry") { viewModel.retry() }
                        .show()
                }
            }
        }
        
        // Observe operation status
        viewModel.operationStatus.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    Toast.makeText(requireContext(), state.data, Toast.LENGTH_SHORT).show()
                    viewModel.clearOperationStatus()
                }
                is UiState.Error -> {
                    Snackbar.make(view, state.message, Snackbar.LENGTH_LONG).show()
                    viewModel.clearOperationStatus()
                }
                else -> {}
            }
        }
        
        // Setup FAB click listener
        addProfileButton = view.findViewById(R.id.add_profile_button)
        addProfileButton.setOnClickListener {
            showAddProfileDialog()
        }
        
        // Fetch profiles from database
        viewModel.fetchProfiles()
    }

    private fun setupEmptyState() {
        emptyStateLayout.findViewById<ImageView>(R.id.emptyStateIcon)
            .setImageResource(android.R.drawable.ic_menu_myplaces)
        emptyStateLayout.findViewById<TextView>(R.id.emptyStateTitle).text = 
            "No Child Profiles"
        emptyStateLayout.findViewById<TextView>(R.id.emptyStateMessage).text = 
            "Add profiles for your children to personalize activity tracking!"
        emptyStateLayout.findViewById<MaterialButton>(R.id.emptyStateButton).apply {
            text = "Add First Profile"
            setOnClickListener { showAddProfileDialog() }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        profilesRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showAddProfileDialog() {
        selectedImageUri = null
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_profile, null)

        val photoPreview = dialogView.findViewById<ImageView>(R.id.profilePhotoPreview)
        val selectPhotoButton = dialogView.findViewById<MaterialButton>(R.id.selectPhotoButton)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.nameInput)
        val ageInput = dialogView.findViewById<TextInputEditText>(R.id.ageInput)
        
        currentPhotoPreview = photoPreview
        
        selectPhotoButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = nameInput.text.toString().trim()
                val ageStr = ageInput.text.toString().trim()

                // Validate inputs
                val nameValidation = ValidationHelper.combine(
                    ValidationHelper.validateNotEmpty(name, "Name"),
                    ValidationHelper.validateMinLength(name, 2, "Name"),
                    ValidationHelper.validateMaxLength(name, 50, "Name")
                )
                
                if (!nameValidation.isSuccess()) {
                    Toast.makeText(requireContext(), nameValidation.getErrorMessage(), Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                val age = ageStr.toIntOrNull()
                val ageValidation = if (age == null) {
                    ValidationResult.Error("Please enter a valid age")
                } else {
                    ValidationHelper.validateAge(age)
                }
                
                if (!ageValidation.isSuccess()) {
                    Toast.makeText(requireContext(), ageValidation.getErrorMessage(), Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                val profile = UserProfile(
                    name = name,
                    age = age!!,
                    photoUrl = selectedImageUri?.toString()
                )
                viewModel.addProfile(profile)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditProfileDialog(profile: UserProfile) {
        selectedImageUri = profile.photoUrl?.let { Uri.parse(it) }
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_profile, null)

        val photoPreview = dialogView.findViewById<ImageView>(R.id.profilePhotoPreview)
        val selectPhotoButton = dialogView.findViewById<MaterialButton>(R.id.selectPhotoButton)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.nameInput)
        val ageInput = dialogView.findViewById<TextInputEditText>(R.id.ageInput)

        // Pre-fill with existing data
        nameInput.setText(profile.name)
        ageInput.setText(profile.age.toString())
        
        // Load existing photo
        profile.photoUrl?.let { uriString ->
            try {
                photoPreview.setImageURI(Uri.parse(uriString))
            } catch (e: Exception) {
                photoPreview.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }
        
        currentPhotoPreview = photoPreview
        
        selectPhotoButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val name = nameInput.text.toString().trim()
                val ageStr = ageInput.text.toString().trim()

                // Validate inputs
                val nameValidation = ValidationHelper.combine(
                    ValidationHelper.validateNotEmpty(name, "Name"),
                    ValidationHelper.validateMinLength(name, 2, "Name"),
                    ValidationHelper.validateMaxLength(name, 50, "Name")
                )
                
                if (!nameValidation.isSuccess()) {
                    Toast.makeText(requireContext(), nameValidation.getErrorMessage(), Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                val age = ageStr.toIntOrNull()
                val ageValidation = if (age == null) {
                    ValidationResult.Error("Please enter a valid age")
                } else {
                    ValidationHelper.validateAge(age)
                }
                
                if (!ageValidation.isSuccess()) {
                    Toast.makeText(requireContext(), ageValidation.getErrorMessage(), Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                val updatedProfile = profile.copy(
                    name = name,
                    age = age!!,
                    photoUrl = selectedImageUri?.toString()
                )
                viewModel.updateProfile(updatedProfile)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmation(profile: UserProfile) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Profile")
            .setMessage("Are you sure you want to delete \"${profile.name}\"'s profile?\n\nThis action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteProfile(profile)
            }
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
}