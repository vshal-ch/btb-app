package com.btb.beatthebox.ui.checkout


import com.btb.beatthebox.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.btb.beatthebox.activities.HomeActivity
import com.btb.beatthebox.data.model.CheckoutRequest
import com.btb.beatthebox.data.model.Note
import com.btb.beatthebox.ui.slot_selection.DateSelectorView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.card.MaterialCardView
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CheckoutFragment : Fragment() {

    private val viewModel: CheckoutViewModel by viewModels()

    private lateinit var btnBack: ImageButton
    private lateinit var payBtn: Button
    private lateinit var tvDate: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvSport: TextView
    private lateinit var tvTotal: TextView
    private lateinit var priceContainer: LinearLayout
    private lateinit var notesContainer: LinearLayout
    private lateinit var shimmerBooking: ShimmerFrameLayout
    private lateinit var shimmerPriceSummary: ShimmerFrameLayout
    private lateinit var shimmerPolicies: ShimmerFrameLayout
    private lateinit var bookingDetailCardView: MaterialCardView
    private lateinit var priceSummaryView: LinearLayout

    private lateinit var view: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        view = inflater.inflate(R.layout.fragment_checkout, container, false)
        shimmerBooking = view.findViewById(R.id.shimmerBooking)
        shimmerPriceSummary = view.findViewById(R.id.shimmerPrice)
        shimmerPolicies = view.findViewById(R.id.shimmerPolicies)
        btnBack = view.findViewById(R.id.btnBack)
        payBtn = view.findViewById(R.id.btnPayNow)
        tvDate = view.findViewById(R.id.tvDate)
        tvTime = view.findViewById(R.id.tvTime)
        tvDuration = view.findViewById(R.id.tvDuration)
        tvSport = view.findViewById(R.id.tvSport)
        tvTotal = view.findViewById(R.id.tvTotal)
        priceContainer = view.findViewById(R.id.priceContainer)
        notesContainer = view.findViewById(R.id.notesContainer)
        bookingDetailCardView = view.findViewById(R.id.bookingDetailCard)
        priceSummaryView = view.findViewById(R.id.priceSummaryView)

        val request =
            requireArguments().getParcelable<CheckoutRequest>("checkoutRequest") ?: return view
        observeViewModel()

        viewModel.verifySlot(request)

        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        payBtn.setOnClickListener {
            viewModel.onPayNow()
        }
        return view
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CheckoutState.Loading -> {
                    startAllShimmers()
                    payBtn.isEnabled = false
                }

                is CheckoutState.VerifyError -> {
                    stopAllShimmers()
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                    payBtn.isEnabled = false
                }

                is CheckoutState.BookingError -> {
                    stopAllShimmers()
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    payBtn.isEnabled = true
                }

                is CheckoutState.SlotVerified -> {
                    stopAllShimmers()
                    val data = state.data
                    tvDate.text = DateSelectorView.formatSelectedDate(
                        LocalDate.parse(
                            data.date,
                            DateTimeFormatter.ISO_DATE
                        )
                    )
                    tvTime.text = timeLabel(data.startTime, data.endTime)
                    tvDuration.text = durationLabel(data.startTime, data.endTime)
                    tvSport.text = "${data.sport} • ${data.box}"

                    priceContainer.removeAllViews()
                    data.breakdown.forEach {
                        priceContainer.addView(priceRow(it.label, it.amount))
                    }

                    tvTotal.text = "₹ ${data.total}"
                    renderNotes(notesContainer, data.policies)
                    payBtn.isEnabled = true
                }

                is CheckoutState.BookingCreated -> {
                    stopAllShimmers()
                    payBtn.isEnabled = false
                    startPayment(state.data.orderId, state.data.paymentSessionId)
                }
            }
        }
    }

    private fun priceRow(label: String, amount: Double): View {
        val view = layoutInflater.inflate(R.layout.item_price_detail, priceContainer, false)
        val tvPriceLabel = view.findViewById<TextView>(R.id.tvPriceLable)
        val tvPriceAmount = view.findViewById<TextView>(R.id.tvPriceAmount)

        tvPriceLabel.text = label
        tvPriceAmount.text = "₹ $amount"
        return view
    }

    private fun renderNotes(
        container: LinearLayout, notes: List<Note>
    ) {
        container.removeAllViews()

        if (notes.isEmpty()) {
            container.visibility = View.GONE
            return
        }

        container.visibility = View.VISIBLE

        notes.forEach { note ->
            val view = layoutInflater.inflate(R.layout.item_policy_checkout, container, false)
            val tvPolicyTitle = view.findViewById<TextView>(R.id.tvPolicyTitle)
            val tvPolicyText = view.findViewById<TextView>(R.id.tvPolicyText)
            val viewMoreButton = view.findViewById<TextView>(R.id.view_more_button)

            tvPolicyTitle.text = note.title
            tvPolicyText.text = note.note

            tvPolicyText.post {
                Log.d("TAG", "renderNotes: ${tvPolicyText.lineCount}")
                if (tvPolicyText.lineCount > 3) {
                    tvPolicyText.maxLines = 3
                    viewMoreButton.visibility = View.VISIBLE
                    viewMoreButton.setOnClickListener {
                        if (tvPolicyText.maxLines == 3) {
                            tvPolicyText.maxLines = Integer.MAX_VALUE // Expand
                            viewMoreButton.text = "View less"
                        } else {
                            tvPolicyText.maxLines = 3 // Collapse
                            viewMoreButton.text = "View more"
                        }
                    }
                }
            }

            container.addView(view)
        }
    }

    private fun startAllShimmers() {
        listOf(shimmerBooking, shimmerPriceSummary, shimmerPolicies).forEach {
            it.startShimmer()
            it.visibility = View.VISIBLE
        }
        listOf(
            bookingDetailCardView,
            priceSummaryView,
            notesContainer
        ).forEach {
            it.visibility = View.GONE
        }
    }

    private fun stopAllShimmers() {
        listOf(shimmerBooking, shimmerPriceSummary, shimmerPolicies).forEach {
            it.stopShimmer()
            it.visibility = View.GONE
        }
        listOf(
            bookingDetailCardView,
            priceSummaryView,
            notesContainer
        ).forEach {
            it.visibility = View.VISIBLE
        }
    }

    private fun timeLabel(startTime: Int, endTime: Int): String {
        fun fmt(min: Int) = java.time.LocalTime.of((min / 60) % 24, min % 60)
            .format(DateTimeFormatter.ofPattern("h:mm a"))

        return "${fmt(unitToMinutes(startTime))} – ${fmt(unitToMinutes(endTime))}"
    }

    private fun durationLabel(startTime: Int, endTime: Int): String {
        val totalMinutes = unitToMinutes(endTime) - unitToMinutes(startTime)
        if (totalMinutes < 0) {
            return "0 min"
        }

        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return when {
            hours == 1 && minutes > 0 -> "$hours hour $minutes min"
            hours > 0 && minutes > 0 -> "$hours hours $minutes min"
            hours == 1 && minutes == 0 -> "$hours hour"
            hours > 1 && minutes == 0 -> "$hours hours"
            hours == 0 && minutes > 0 -> "$minutes min"
            else -> "0 min" // Handles case where totalMinutes is 0
        }
    }

    private fun unitToMinutes(unit: Int): Int {
        return (unit + 12) * 30
    }

    private fun startPayment(orderId: String, sessionId: String) {
        val activity = requireActivity() as HomeActivity
        activity.startCashfreePayment(orderId, sessionId)
    }

}
