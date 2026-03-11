package com.btb.beatthebox.ui.availability

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
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
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.btb.beatthebox.R
import com.btb.beatthebox.data.model.Slot
import com.btb.beatthebox.ui.shared.MainViewModel
import com.btb.beatthebox.utils.ApiResult
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class SlotAvailabilityFragment : Fragment() {

    private val slotViewModel: SlotAvailabilityViewModel by viewModels()

    private lateinit var tvVenueName: TextView
    private lateinit var spinnerDay: Spinner
    private lateinit var swipeLayout: SwipeRefreshLayout
    private lateinit var slotOverlay: FrameLayout
    private lateinit var boxToggleGroup: MaterialButtonToggleGroup
    private lateinit var btnBookNow: MaterialButton

    private lateinit var loaderContainer: View
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var root: View

    private var selectedDay: String = "Today" // Keep track of current selection
    private var selectedBox: String = "Box-1"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_slot_availability, container, false)

        tvVenueName = root.findViewById(R.id.header_title)
        spinnerDay = root.findViewById(R.id.spinnerDate)
        swipeLayout = root.findViewById(R.id.swipeRefresh_availability)
        loaderContainer = root.findViewById(R.id.loader_container)
        progressBar = root.findViewById(R.id.progress_bar)
        errorText = root.findViewById(R.id.error_text)
        slotOverlay = root.findViewById(R.id.slotOverlay)
        boxToggleGroup = root.findViewById(R.id.boxToggleGroup)
        btnBookNow = root.findViewById(R.id.ctaBookNow_avail)

        setupSpinner()
        setUpBoxToggleGroup()
        observeViewModels()

        // Refresh button click
        swipeLayout.setOnRefreshListener {
            slotViewModel.refreshSlots(convertDayToDate(selectedDay), selectedBox)
        }

        slotViewModel.fetchSlotsIfNeeded(convertDayToDate(selectedDay), selectedBox)

        btnBookNow.setOnClickListener {
            findNavController().navigate(R.id.action_availability_to_slotSelection)
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
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                selectedDay = items[position]
                slotViewModel.setLoaded(false)
                slotViewModel.fetchSlotsIfNeeded(
                    convertDayToDate(selectedDay), selectedBox
                ) // Fetch slots immediately when user changes selection
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun observeViewModels() {
        // Observe Slots
        slotViewModel.slots.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is ApiResult.Success -> {
                    loaderContainer.visibility = View.GONE
                    swipeLayout.isRefreshing = false
                    progressBar.visibility = View.GONE
                    errorText.visibility = View.GONE

                    val density = resources.displayMetrics.density
                    val slots: List<Slot> = result.data
                    slotOverlay.removeAllViews()
                    slotOverlay.post {
                        val parentWidth = slotOverlay.width
                        val rightWidth = (parentWidth * 0.80f).toInt()
                        slots.forEach { slot ->
                            val startDp = (slot.startTime.time - 360) * 1f    // 1 minute = 1dp
                            val heightDp = (slot.endTime.time - slot.startTime.time) * 1f
                            //added 20 to match the padding of the linear layout parent element
                            val startPx = ((startDp + 20) * density).toInt()
                            val heightPx = (heightDp * density).toInt()

                            val blockView =
                                layoutInflater.inflate(R.layout.slot_block, slotOverlay, false)

                            blockView.findViewById<TextView>(R.id.tvStart).text =
                                formatHourWithMins(slot.startTime.time)
                            blockView.findViewById<TextView>(R.id.tvSport).text = slot.sport
                            blockView.findViewById<TextView>(R.id.tvEnd).text =
                                formatHourWithMins(slot.endTime.time)

                            val params = FrameLayout.LayoutParams(
                                rightWidth,
                                heightPx,
                            ).apply {
                                topMargin = startPx
                                gravity = Gravity.END
                                rightMargin = 12
                            }

                            slotOverlay.addView(blockView, params)
                        }
                    }
                }

                is ApiResult.Error -> {
                    swipeLayout.isRefreshing = false
                    loaderContainer.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    errorText.visibility = View.VISIBLE
                    errorText.text = result.message
                    Toast.makeText(
                        requireContext(), "Failed: ${result.message}", Toast.LENGTH_SHORT
                    ).show()
                }

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
            }
        })
    }

    private fun convertDayToDate(day: String): String {
        val date: String
        var today = LocalDate.now()

        when (day) {
            "Today" -> {
                date = today.toString()
            }

            "Tomorrow" -> {
                date = today.plusDays(1).toString()
            }

            "Day After" -> {
                date = today.plusDays(2).toString()
            }

            else -> {
                date = today.toString()
            }
        }
        return date
    }

    companion object {
        fun formatHourWithMins(minutes: Int): String {
            val hour = minutes / 60
            val mins = minutes % 60
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            val suffix = if (hour >= 12) "PM" else "AM"
            return "${String.format("%02d:%02d", displayHour, mins)} $suffix"
        }
    }

    private fun setUpBoxToggleGroup() {
        boxToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            selectedBox = when (checkedId) {
                R.id.btnBox1 -> "Box-1"
                R.id.btnBox2 -> "Box-2"
                else -> return@addOnButtonCheckedListener
            }

            updateBoxLabels(selectedBox)

            slotViewModel.setLoaded(false)
            slotViewModel.fetchSlotsIfNeeded(
                convertDayToDate(selectedDay), selectedBox
            )
        }
    }

    private fun updateBoxLabels(selectedBox: String) {
        root.findViewById<TextView>(R.id.tvBox1Label).alpha =
            if (selectedBox == "Box-1") 1f else 0.5f
        root.findViewById<TextView>(R.id.tvBox2Label).alpha =
            if (selectedBox == "Box-2") 1f else 0.5f
    }

    val Int.time: Int
        get() = ((this + 12) * 30)
}
