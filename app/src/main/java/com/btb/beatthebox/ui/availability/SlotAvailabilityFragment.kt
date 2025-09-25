package com.btb.beatthebox.ui.availability

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.btb.beatthebox.R
import com.btb.beatthebox.ui.shared.MainViewModel
import com.btb.beatthebox.utils.ApiResult
import java.util.Calendar

class SlotAvailabilityFragment : Fragment() {

    private val slotViewModel: SlotAvailabilityViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    private lateinit var tvVenueName: TextView
    private lateinit var tvVenueStatus: TextView
    private lateinit var tvVenueTimings: TextView
    private lateinit var spinnerDay: Spinner
    private lateinit var btnRefresh: ImageButton
    private lateinit var recyclerSlots: RecyclerView
    private lateinit var adapter: SlotsAdapter

    private lateinit var loaderContainer: View
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView


    private var selectedDay: String = "Today" // Keep track of current selection

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_slot_availability, container, false)

        tvVenueName = root.findViewById(R.id.header_title)
        tvVenueStatus = root.findViewById(R.id.tvVenueStatus)
        tvVenueTimings = root.findViewById(R.id.tvVenueTimings)
        spinnerDay = root.findViewById(R.id.spinnerDate)
        btnRefresh = root.findViewById(R.id.btnRefresh)
        recyclerSlots = root.findViewById(R.id.rvTimeline)
        loaderContainer = root.findViewById(R.id.loader_container)
        progressBar = root.findViewById(R.id.progress_bar)
        errorText = root.findViewById(R.id.error_text)

        recyclerSlots.layoutManager = LinearLayoutManager(requireContext())
        adapter = SlotsAdapter()
        val dividerItemDecoration = DividerItemDecoration(
            recyclerSlots.context,
            LinearLayoutManager.VERTICAL
        )
//        recyclerSlots.addItemDecoration(dividerItemDecoration)
//        val hourLabelWidth =  (64 * resources.displayMetrics.density).toInt()
        recyclerSlots.addItemDecoration(
            ColoredDivider(
                requireContext(),
                ContextCompat.getColor(requireContext(), R.color.divider_color),
                thickness = 2,   // divider thickness in px
                marginStartDp = 60 // same as your paddingStart
            )
        )
        recyclerSlots.adapter = adapter

        setupSpinner()
        observeViewModels()

        // Refresh button click
        btnRefresh.setOnClickListener {
            fetchSlotsForDay(selectedDay)
        }

        return root
    }

    private fun setupSpinner() {
        val items = listOf("Today", "Tomorrow", "Day After")
        val arrayAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDay.adapter = arrayAdapter

        // Handle selection change
        spinnerDay.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedDay = items[position]
                fetchSlotsForDay(selectedDay) // Fetch slots immediately when user changes selection
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun observeViewModels() {
        // Observe Venue details from MainViewModel
        mainViewModel.venueDetails.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is ApiResult.Success -> {
                    val venue = result.data
                    tvVenueName.text = venue.name
                    val drawable = tvVenueStatus.compoundDrawablesRelative[0]

                    if (venue.isOpen) {
                        val calendar = Calendar.getInstance()
                        val currentHour = calendar.get(Calendar.HOUR_OF_DAY) // 24-hour format

                        if (currentHour < 6) { // Between 12 AM and 6 AM
                            tvVenueStatus.text = "Closed"
                            tvVenueStatus.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.closed_status_color
                                )
                            )
                            drawable?.setTint(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.closed_status_color
                                )
                            )
                            tvVenueTimings.text = "Opens at 6 AM"
                        } else {
                            tvVenueStatus.text = "Open"
                            tvVenueStatus.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.open_status_color
                                )
                            )
                            drawable?.setTint(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.open_status_color
                                )
                            )
                            tvVenueTimings.text = venue.timings
                        }
                    } else {
                        tvVenueStatus.text = "Closed"
                        tvVenueStatus.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.closed_status_color
                            )
                        )
                        drawable?.setTint(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.closed_status_color
                            )
                        )
                        tvVenueTimings.visibility = View.GONE
                    }
                }

                else -> {}
            }
        })

        // Observe Slots
        slotViewModel.slots.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is ApiResult.Success -> {
                    loaderContainer.visibility = View.GONE
                    adapter.setBlockedSlots(result.data) // Paint blocked slots
                }

                is ApiResult.Error -> {
                    loaderContainer.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    errorText.visibility = View.VISIBLE
                    errorText.text = result.message
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
                    // Optional: Show loading indicator
                }
            }
        })
    }

    private fun fetchSlotsForDay(day: String) {
        val calendar = java.util.Calendar.getInstance()
        var date: String
        var currentTimeMinutes: Int

        when (day) {
            "Today" -> {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                date = sdf.format(calendar.time)
                currentTimeMinutes = 360
//                TODO set this and add an overlay over the timeline
//                val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
//                val minute = calendar.get(java.util.Calendar.MINUTE)
//                currentTimeMinutes = hour * 60 + minute
            }

            "Tomorrow" -> {
                calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                date = sdf.format(calendar.time)
                currentTimeMinutes = 360 // 6:00 AM
            }

            "Day After" -> {
                calendar.add(java.util.Calendar.DAY_OF_YEAR, 2)
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                date = sdf.format(calendar.time)
                currentTimeMinutes = 360 // 6:00 AM
            }

            else -> {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                date = sdf.format(calendar.time)
                currentTimeMinutes = 360
            }
        }

        slotViewModel.fetchSlots(date, currentTimeMinutes)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // First API call after splash → default "Today"
//        fetchSlotsForDay(selectedDay)
    }
}
