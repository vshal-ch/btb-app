package com.btb.beatthebox.ui.user_profile

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.btb.beatthebox.R
import com.btb.beatthebox.data.state.UserSession
import com.btb.beatthebox.data.viewmodel.UserViewModel
import com.btb.beatthebox.di.ServiceLocator
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class EditProfileBottomSheet :
    BottomSheetDialogFragment(R.layout.bottom_sheet_edit_profile) {

    companion object {
        private const val ARG_FIELD = "field"

        fun newInstance(field: EditProfileField): EditProfileBottomSheet {
            val sheet = EditProfileBottomSheet()
            sheet.arguments = Bundle().apply {
                putString(ARG_FIELD, field.name)
            }
            return sheet
        }
    }

    private lateinit var field: EditProfileField
    private lateinit var userViewModel: UserViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        field = EditProfileField.valueOf(
            requireArguments().getString(ARG_FIELD)!!
        )

        userViewModel =
            ViewModelProvider(requireActivity())[UserViewModel::class.java]

        val title = view.findViewById<TextView>(R.id.title)

        val nameLayout = view.findViewById<TextInputLayout>(R.id.nameLayout)
        val nameInput = view.findViewById<TextInputEditText>(R.id.nameInput)

        val phoneLayout = view.findViewById<TextInputLayout>(R.id.phoneLayout)
        val phoneInput = view.findViewById<TextInputEditText>(R.id.phoneInput)

        val saveBtn = view.findViewById<View>(R.id.saveBtn)

        val user = UserSession.user.value

        when (field) {
            EditProfileField.NAME -> {
                title.text = "Edit name"
                nameLayout.visibility = View.VISIBLE
                phoneLayout.visibility = View.GONE
                nameInput.setText(user?.name.orEmpty())
            }

            EditProfileField.PHONE -> {
                title.text = "Edit phone number"
                phoneLayout.visibility = View.VISIBLE
                nameLayout.visibility = View.GONE
                phoneInput.setText(user?.phoneNo.orEmpty())
            }
        }

        saveBtn.setOnClickListener {
            when (field) {
                EditProfileField.NAME -> submitName(nameInput, nameLayout)
                EditProfileField.PHONE -> submitPhone(phoneInput, phoneLayout)
            }
        }
    }

    private fun submitName(
        input: TextInputEditText,
        layout: TextInputLayout
    ) {
        val name = input.text?.toString()?.trim().orEmpty()

        if (name.length < 2) {
            layout.error = "Name must be at least 2 characters"
            return
        }

        lifecycleScope.launch {
            try {
                val updatedUser =
                    ServiceLocator.authRepository.completeProfile(
                        name = name,
                        phone = null
                    )
                UserSession.setUser(updatedUser)
                dismiss()
            } catch (e: Exception) {
                layout.error = e.localizedMessage ?: "Failed to update name"
            }
        }
    }

    private fun submitPhone(
        input: TextInputEditText,
        layout: TextInputLayout
    ) {
        val phone = input.text?.toString()?.trim().orEmpty()

        if (phone.length < 10) {
            layout.error = "Invalid phone number"
            return
        }

        lifecycleScope.launch {
            try {
                val updatedUser =
                    ServiceLocator.authRepository.completeProfile(
                        name = null,
                        phone = phone
                    )
                UserSession.setUser(updatedUser)
                dismiss()
            } catch (e: Exception) {
                layout.error = e.localizedMessage ?: "Failed to update phone"
            }
        }
    }
}
