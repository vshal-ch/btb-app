package com.btb.beatthebox.ui.user_profile

import android.content.Intent
import com.btb.beatthebox.R
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.btb.beatthebox.activities.LoginActivity
import com.btb.beatthebox.activities.SettingsActivity
import com.btb.beatthebox.data.viewmodel.UserViewModel
import com.btb.beatthebox.ui.common.SimpleDividerItemDecoration
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    private val userViewModel: UserViewModel by lazy {
        ViewModelProvider(requireActivity())[UserViewModel::class.java]
    }

    private lateinit var profileAdapter: ProfileAdapter
    private lateinit var progressBar: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val headerName = view.findViewById<TextView>(R.id.headerName)
        val editName = view.findViewById<ImageView>(R.id.editName)
        val avatarView = view.findViewById<ImageView>(R.id.avatar)
        progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)

        profileAdapter = ProfileAdapter()

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = profileAdapter

        recyclerView.addItemDecoration(
            SimpleDividerItemDecoration(requireContext())
        )

        editName.setOnClickListener {
            EditProfileBottomSheet
                .newInstance(EditProfileField.NAME)
                .show(childFragmentManager, "edit_name")
        }
        setupAdapterCallbacks()
        observeUser(headerName, avatarView)
    }

    private fun setupAdapterCallbacks() {
        profileAdapter.onEditPhone = {
            EditProfileBottomSheet
                .newInstance(EditProfileField.PHONE)
                .show(childFragmentManager, "edit_phone")
        }

        profileAdapter.onActionClick = { label ->
            when (label) {
                "Settings" -> {
                    startActivity(
                        Intent(requireContext(), SettingsActivity::class.java)
                    )
                }

                "Booking History" -> {
                    findNavController().navigate(R.id.action_userProfile_to_userGames)
                }

                "FAQ & Help" -> {
                    findNavController().navigate(R.id.action_userProfile_to_faq)
                }

                "Privacy & Others" -> {
                    findNavController().navigate(R.id.action_userProfile_to_policies)
                }
            }
        }

        profileAdapter.onLogout = {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                progressBar.visibility = View.VISIBLE
                userViewModel.logout {
                    progressBar.visibility = View.GONE
                    navigateToLogin()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun observeUser(headerName: TextView, avatarView: ImageView) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                androidx.lifecycle.Lifecycle.State.STARTED
            ) {
                userViewModel.user.collect { user ->
                    user ?: return@collect

                    headerName.text = user.name
                    Glide.with(this@UserProfileFragment)
                        .load(user.avatarUrl)
                        .placeholder(R.drawable.avatar_placeholder)
                        .circleCrop()
                        .into(avatarView)
                    profileAdapter.items = ProfileUiMapper.map(user)
                    profileAdapter.notifyDataSetChanged()
                }
            }
        }
    }
}
