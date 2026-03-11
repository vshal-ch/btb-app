package com.btb.beatthebox.ui.reschedule

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.btb.beatthebox.R
import com.btb.beatthebox.data.model.GamePass
import com.btb.beatthebox.ui.slot_selection.BookingSummary
import com.btb.beatthebox.ui.slot_selection.BookingSummaryView
import com.btb.beatthebox.ui.slot_selection.DateSelectorView
import com.btb.beatthebox.ui.slot_selection.timeline.TimelineView
import com.btb.beatthebox.utils.ApiResult
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RescheduleFragment : Fragment() {

    private val viewModel: RescheduleViewModel by viewModels {
        RescheduleViewModel.RescheduleViewModelFactory(game)
    }

    private lateinit var dateSelector: DateSelectorView
    private lateinit var timelineView: TimelineView
    private lateinit var fromSummaryView: BookingSummaryView
    private lateinit var toSummaryView: BookingSummaryView
    private lateinit var btnReschedule: Button
    private lateinit var tvErrorMsg: TextView
    private lateinit var btnInfo: ImageButton

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var loaderContainer: FrameLayout

    private lateinit var game: GamePass

    private var isFirstTime = true
    private var policyShown = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_reschedule, container, false)

        dateSelector = root.findViewById(R.id.dateSelectorView)
        timelineView = root.findViewById(R.id.timelineView)
        fromSummaryView = root.findViewById(R.id.fromSummaryView)
        toSummaryView = root.findViewById(R.id.toSummaryView)
        swipeRefresh = root.findViewById(R.id.swipeRefresh)
        progressBar = root.findViewById(R.id.progress_bar)
        errorText = root.findViewById(R.id.error_text)
        loaderContainer = root.findViewById(R.id.loader_container)
        btnReschedule = root.findViewById(R.id.btnReschedule)
        tvErrorMsg = root.findViewById(R.id.errorText)
        btnInfo = root.findViewById(R.id.btnInfo)

        game = requireArguments().getParcelable("game") ?: return root
        isFirstTime = true

        swipeRefresh.setOnRefreshListener {
            viewModel.fetchSlots()
        }
        btnInfo.setOnClickListener {
            val notes = viewModel.policyNotes.value ?: return@setOnClickListener
            ReschedulePolicyBottomSheet(notes)
                .show(parentFragmentManager, "reschedule_policy_manual")
        }

        btnReschedule.setOnClickListener {
            viewModel.submitReschedule()
        }

        observeViewModel()
        return root
    }

    private fun observeViewModel() {
        viewModel.blockedSlots.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ApiResult.Loading,
                is ApiResult.Refreshing
                    -> {
                    showLoading()
                }

                is ApiResult.Success -> {
                    hideLoading()
                    setupTimelineView()
                    timelineView.setBlockedSlots(
                        state.data,
                        viewModel.getSelectedDate()
                    )
                    timelineView.setStartUnit(game.startTime, game.endTime  )

                    if (isFirstTime) {
                        setupFromSummaryView()
                        setupDateSelector()
                        this@RescheduleFragment.isFirstTime = false
                    }
                }

                is ApiResult.Error -> {
                    showError(state.message)
                }
            }
        }

        viewModel.toSummaryState.observe(viewLifecycleOwner) { summary ->

            when (summary) {

                is SummaryState.SummaryError -> {
                    toSummaryView.showMessage(summary.message)
                    btnReschedule.isEnabled = false
                }

                is SummaryState.Summary -> {
                    toSummaryView.showSummary(summary.summary)
                    if (summary.error != null) {
                        tvErrorMsg.text = summary.error
                        btnReschedule.isEnabled = false
                    } else {
                        tvErrorMsg.text = ""
                        btnReschedule.isEnabled = true
                    }
                }

                else -> {
                    toSummaryView.showMessage("Select a slot to see summary")
                    btnReschedule.isEnabled = false
                }
            }

        }

        viewModel.policyNotes.observe(viewLifecycleOwner) { notes ->
            Log.d("RescheduleFragment", "Notes: $notes")
            if (!notes.isNullOrEmpty() && !policyShown) {
                policyShown = true
                ReschedulePolicyBottomSheet(notes)
                    .show(parentFragmentManager, "reschedule_policy")
            }
        }

        viewModel.rescheduleState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RescheduleActionState.Idle -> {

                }

                is RescheduleActionState.Loading -> {
                    showLoading()
                }

                is RescheduleActionState.Error -> {
                    hideLoading()
                    Toast.makeText(
                        requireContext(),
                        state.message,
                        Toast.LENGTH_LONG
                    ).show()
                }

                is RescheduleActionState.Success -> {
                    hideLoading()
                    navigateToUpdatedGamePass(state.newBookingId)
                }
            }
        }

    }

    private fun setupDateSelector() {
        dateSelector.setDate(viewModel.getSelectedDate())
        dateSelector.onDateSelected = { date ->
            viewModel.onDateSelected(date)
        }
    }

    private fun setupTimelineView() {
//        timelineView.setDuration((game.endTime - game.startTime) * 30)
        timelineView.onSelectionChanged = { startUnit, endUnit ->
            viewModel.onSlotSelected(startUnit, endUnit)
        }
    }

    private fun setupFromSummaryView() {
        fromSummaryView.showSummary(convertGameToBookingSummary(game))
    }

    private fun convertGameToBookingSummary(game: GamePass): BookingSummary {
        val bookingSummary = BookingSummary(
            LocalDate.parse(game.date, DateTimeFormatter.ISO_DATE),
            game.sport,
            game.startTime,
            game.endTime,
            (game.endTime - game.startTime) * 30,
            viewModel.getFromPriceWithoutDiscount(),
            game.box
        )
        return bookingSummary
    }

    private fun showLoading() {
        loaderContainer.visibility = View.VISIBLE
        swipeRefresh.isRefreshing = false
        progressBar.visibility = View.VISIBLE
        errorText.visibility = View.GONE
        btnReschedule.isEnabled = false
    }

    private fun showError(message: String) {
        swipeRefresh.isRefreshing = false
        loaderContainer.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        errorText.visibility = View.VISIBLE
        errorText.text = message
    }

    private fun hideLoading() {
        loaderContainer.visibility = View.GONE
        swipeRefresh.isRefreshing = false
        progressBar.visibility = View.GONE
        errorText.visibility = View.GONE
        btnReschedule.isEnabled = true
    }

    private fun navigateToUpdatedGamePass(newBookingId: String) {
        findNavController().popBackStack(
            R.id.navigation_home,
            false // remove old GamePass
        )

        findNavController().navigate(
            R.id.bookingPassFragment,
            bundleOf("bookingId" to newBookingId)
        )
    }

}