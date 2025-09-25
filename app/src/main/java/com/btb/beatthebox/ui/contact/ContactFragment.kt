package com.btb.beatthebox.ui.contact

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.btb.beatthebox.R
import com.btb.beatthebox.utils.ApiResult


class ContactFragment : Fragment(R.layout.fragment_contact) {

    private val viewModel: ContactViewModel by viewModels()
    private lateinit var adapter: ContactAdapter
    private lateinit var rvContacts: RecyclerView

    private lateinit var loaderContainer: View
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rvContacts = view.findViewById(R.id.rvContacts)
        loaderContainer = view.findViewById(R.id.loader_container)
        progressBar = view.findViewById(R.id.progress_bar)
        errorText = view.findViewById(R.id.error_text)

        adapter = ContactAdapter(
            contacts = emptyList(),
            onEmailClick = { email -> openEmail(email) },
            onPhoneClick = { number -> dialNumber(number) },
            onWhatsAppClick = { number -> openWhatsApp(number) }
        )

        rvContacts.layoutManager = LinearLayoutManager(requireContext())
        rvContacts.adapter = adapter

        viewModel.contacts.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ApiResult.Success -> {
                    loaderContainer.visibility = View.GONE
                    adapter = ContactAdapter(result.data, ::openEmail, ::dialNumber, ::openWhatsApp)
                    rvContacts.adapter = adapter
                }

                is ApiResult.Error -> {
                    loaderContainer.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    errorText.text = result.message
                    errorText.visibility = View.VISIBLE
                    Toast.makeText(
                        requireContext(),
                        "Failed: ${result.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is ApiResult.Loading -> {
                    loaderContainer.visibility = View.VISIBLE
                    progressBar.visibility = View.VISIBLE
                    errorText.visibility = View.GONE
                }
            }
        }

        viewModel.loadContacts()
    }

    private fun openEmail(email: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:$email".toUri()
        }
        startActivity(intent)
    }

    private fun dialNumber(number: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = "tel:$number".toUri()
        }
        startActivity(intent)
    }

    private fun openWhatsApp(number: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "https://wa.me/$number".toUri()
        }
        startActivity(intent)
    }
}

