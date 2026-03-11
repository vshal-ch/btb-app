package com.btb.beatthebox.ui.faq

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.btb.beatthebox.R
import com.btb.beatthebox.data.repository.CostRepository
import com.btb.beatthebox.data.repository.FaqRepository
import com.btb.beatthebox.data.viewmodel.SportCostViewModel
import com.btb.beatthebox.di.ServiceLocator
import com.btb.beatthebox.di.ServiceLocator.apiService
import com.btb.beatthebox.utils.ApiResult
import androidx.core.net.toUri

class FaqFragment : Fragment(R.layout.fragment_faq) {

    private val viewModel: FaqViewModel by viewModels {
        ViewModelProvider(
            this,
            FaqViewModel.Factory(
                FaqRepository(apiService)
            )
        )[FaqViewModel::class.java]
    }

    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var errorLayout: View
    private lateinit var errorText: TextView
    private lateinit var retryButton: Button

    private lateinit var adapter: FaqAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views manually
        progressBar = view.findViewById(R.id.progressBar)
        recyclerView = view.findViewById(R.id.rvFaqs)
        errorLayout = view.findViewById(R.id.errorLayout)
        errorText = view.findViewById(R.id.tvError)
        retryButton = view.findViewById(R.id.btnRetry)
        val contactText = view.findViewById<TextView>(R.id.tvContact)

        contactText.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:beatthebox.btb@gmail.com".toUri()
                putExtra(Intent.EXTRA_SUBJECT, "Help / Feedback")
            }
            startActivity(intent)
        }

        setupRecyclerView()
        observeState()

        retryButton.setOnClickListener {
            viewModel.loadFaqs()
        }

        viewModel.loadFaqs()
    }

    private fun setupRecyclerView() {
        adapter = FaqAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            progressBar.visibility = View.GONE
            recyclerView.visibility = View.GONE
            errorLayout.visibility = View.GONE

            when (state) {
                is ApiResult.Loading,is ApiResult.Refreshing -> {
                    progressBar.visibility = View.VISIBLE
                }
                is ApiResult.Success -> {
                    recyclerView.visibility = View.VISIBLE
                    adapter.submitList(state.data)
                }
                is ApiResult.Error -> {
                    errorLayout.visibility = View.VISIBLE
                    errorText.text = state.message
                }
            }
        }
    }
}
