package com.btb.beatthebox.ui.slot_selection

import android.graphics.Typeface
import com.btb.beatthebox.R
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BulletSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.btb.beatthebox.data.model.Note
import com.btb.beatthebox.ui.reschedule.SummaryState
import com.btb.beatthebox.ui.slot_selection.timeline.TimelineView

class SlotSelectionFragment : Fragment(R.layout.fragment_slot_selection) {

    private val viewModel: SlotSelectionViewModel by viewModels()

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var timelineView: TimelineView
    private lateinit var durationSelectorView: DurationSelectorView
    private lateinit var bookNowBtn: Button
    private lateinit var tvNotes: TextView
    private lateinit var tvNotesHeader: TextView

    private lateinit var dateButtonContainer: LinearLayout

    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var warningText: TextView
    private lateinit var loaderContainer: FrameLayout
    private lateinit var contentContainer: View

    private lateinit var sportAdapter: SportSelectorAdapter
    private lateinit var dateSelectorView: DateSelectorView
    private lateinit var summaryView: BookingSummaryView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        timelineView = view.findViewById(R.id.timelineView)
        durationSelectorView = view.findViewById(R.id.durationSelector)
        bookNowBtn = view.findViewById(R.id.btnBookNow)
        dateButtonContainer = view.findViewById(R.id.dateButtonContainer)
        progressBar = view.findViewById(R.id.progress_bar)
        errorText = view.findViewById(R.id.error_text)
        warningText = view.findViewById(R.id.warningText)
        loaderContainer = view.findViewById(R.id.loader_container)
        contentContainer = view.findViewById(R.id.content_container)
        timelineView = view.findViewById(R.id.timelineView)
        summaryView = view.findViewById(R.id.summaryView)
        tvNotes = view.findViewById(R.id.tvNotes)
        tvNotesHeader = view.findViewById(R.id.tvNotesHeader)
        dateSelectorView = view.findViewById(R.id.dateSelectorView)

        observeState()
        setupDateSelector()
        setupSportSelector(view)
        setupDurationSelectorView()
        setupTimeline()
        setupBookNowListener()

        swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
        if (viewModel.getSelectedSport() != null) {
            viewModel.refresh()
        }
    }

    private fun observeState() {

        viewModel.uiState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is SlotSelectionState.Loading -> {
                    showLoading()
                }

                is SlotSelectionState.Refreshing -> {
                    showRefreshing()
                }

                is SlotSelectionState.Success -> {
                    hideLoading()
                    sportAdapter.submit(result.sportDetails)
                    timelineView.setBlockedSlots(result.slots, viewModel.getSelectedDate())
                }

                is SlotSelectionState.Error -> {
                    showError(result.message)
                }
            }
        }

        viewModel.summary.observe(viewLifecycleOwner) { summary ->
            when (summary) {

                is SummaryState.SummaryError -> {
                    summaryView.showMessage(summary.message)
                    bookNowBtn.isEnabled = false
                    warningText.text = ""
                }

                is SummaryState.Summary -> {
                    summaryView.showSummary(summary.summary)
                    bookNowBtn.isEnabled = true
                    warningText.text = summary.warning ?: ""
                }

                else -> {
                    summaryView.showMessage("Select a slot to see summary")
                    warningText.text = ""
                    bookNowBtn.isEnabled = false
                }
            }
        }

        viewModel.notesLive.observe(viewLifecycleOwner) { notes ->
            if (notes.isNullOrEmpty()) {
                tvNotes.visibility = View.GONE
                tvNotesHeader.visibility = View.GONE
                return@observe
            }
            tvNotes.visibility = View.VISIBLE
            tvNotesHeader.visibility = View.VISIBLE
            tvNotesHeader.text = "Notes"
            tvNotes.text = notesToSpannable(notes)
        }

    }

    private fun setupDateSelector() {
        dateSelectorView.setDate(viewModel.getSelectedDate())
        dateSelectorView.onDateSelected = { date ->
            viewModel.onDateSelected(date)
        }
    }

    private fun setupSportSelector(view: View) {
        val rv = view.findViewById<RecyclerView>(R.id.rvSports)
        sportAdapter = SportSelectorAdapter { sport ->
            viewModel.onSportSelected(sport)
        }
        rv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        val selected = viewModel.getSelectedSport()
        if (selected != null) {
            sportAdapter.setSelectedSport(selected)
        }
        rv.adapter = sportAdapter

    }

    private fun setupDurationSelectorView() {
        durationSelectorView.onDurationChanged = { duration ->
            timelineView.setDuration(duration)
        }
    }

    private fun setupTimeline() {
        timelineView.onSelectionChanged = { startUnit, endUnit ->
            viewModel.onSlotSelected(startUnit, endUnit)
        }
    }

    private fun setupBookNowListener() {
        bookNowBtn.setOnClickListener {
            navigateToCheckout()
        }
    }

    private fun navigateToCheckout() {
        val checkoutRequest = viewModel.getCheckoutRequest()
        if (checkoutRequest == null) {
            Toast.makeText(requireContext(), "Invalid slot selection.", Toast.LENGTH_SHORT).show()
            return
        }
        val bundle = bundleOf("checkoutRequest" to checkoutRequest)
        findNavController().navigate(R.id.action_slotSelection_to_checkout, bundle)
    }

    companion object {
        fun notesToSpannable(notes: List<Note>): CharSequence {
            if (notes.isEmpty()) return ""

            val bulletGap = 16  // px, adjust if needed
            val builder = SpannableStringBuilder()

            notes.groupBy { it.title }.forEach { (title, items) ->
                // Title
                val titleStart = builder.length
                builder.append(title).append("\n")
                builder.setSpan(
                    StyleSpan(Typeface.BOLD),
                    titleStart,
                    builder.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // Bullets
                items.forEach { note ->
                    val start = builder.length
                    builder.append(note.note).append("\n")

                    builder.setSpan(
                        BulletSpan(bulletGap),
                        start,
                        builder.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                builder.append("\n")
            }

            return builder.trim()
        }
    }

    private fun showLoading() {
        loaderContainer.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        errorText.visibility = View.GONE
        swipeRefresh.isRefreshing = false
    }

    private fun showRefreshing() {
        loaderContainer.visibility = View.GONE
        progressBar.visibility = View.GONE
        errorText.visibility = View.GONE
        swipeRefresh.isRefreshing = true
    }

    private fun showError(message: String) {
        loaderContainer.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        swipeRefresh.isRefreshing = false
        errorText.visibility = View.VISIBLE
        errorText.text = message
    }

    private fun hideLoading() {
        loaderContainer.visibility = View.GONE
        progressBar.visibility = View.GONE
        errorText.visibility = View.GONE
        swipeRefresh.isRefreshing = false
    }

}

