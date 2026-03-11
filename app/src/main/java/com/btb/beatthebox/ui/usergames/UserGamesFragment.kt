package com.btb.beatthebox.ui.usergames

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.btb.beatthebox.R
import com.btb.beatthebox.utils.ApiResult
import com.google.android.material.chip.ChipGroup

class UserGamesFragment : Fragment(R.layout.fragment_games) {

    private val viewModel: GamesViewModel by viewModels()
    private lateinit var adapter: GamesAdapter
    private lateinit var swipeLayout: SwipeRefreshLayout
    private lateinit var loaderContainer: View
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        swipeLayout = view.findViewById(R.id.swipeRefresh_availability)
        loaderContainer = view.findViewById(R.id.loader_container)
        progressBar = view.findViewById(R.id.progress_bar)
        errorText = view.findViewById(R.id.error_text)
        adapter = GamesAdapter {
            findNavController().navigate(
                R.id.action_userGamesFragment_to_bookingPassFragment,
                bundleOf("bookingId" to it.id)
            )
        }

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerBookings)
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(requireContext())

        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (!rv.canScrollVertically(1)) {
                    viewModel.load(currentFilter())
                }
            }
        })

        swipeLayout.setOnRefreshListener {
            viewModel.load(currentFilter(), reset = true)
        }

        setupFilters(view)
        observeState()

        viewModel.load("all")
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                is ApiResult.Loading -> {
                    loaderContainer.visibility = View.VISIBLE
                    progressBar.visibility = View.VISIBLE
                    errorText.visibility = View.GONE
                    swipeLayout.isRefreshing = false
                }

                is ApiResult.Refreshing -> {
                    loaderContainer.visibility = View.GONE
                    progressBar.visibility = View.VISIBLE
                    errorText.visibility = View.GONE
                    swipeLayout.isRefreshing = true
                }

                is ApiResult.Success -> {
                    loaderContainer.visibility = View.GONE
                    swipeLayout.isRefreshing = false
                    progressBar.visibility = View.GONE
                    errorText.visibility = View.GONE
                    adapter.submitList(it.data)
                }

                is ApiResult.Error -> {
                    swipeLayout.isRefreshing = false
                    loaderContainer.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    errorText.visibility = View.VISIBLE
                    errorText.text = it.message
                }
            }
        }
    }

    private fun setupFilters(view: View) {
        val chipGroup = view.findViewById<ChipGroup>(R.id.filterGroup)

        chipGroup.setOnCheckedChangeListener { _, checkedId ->
            val filter = when (checkedId) {
                R.id.chipFuture -> "future"
                R.id.chipPast -> "past"
                else -> "all"
            }
            viewModel.load(filter, reset = true)
        }
    }


    private fun currentFilter(): String {
        return when (
            view?.findViewById<ChipGroup>(R.id.filterGroup)
                ?.checkedChipId
        ) {
            R.id.chipFuture -> "future"
            R.id.chipPast -> "past"
            else -> "all"
        }
    }
}
