package com.btb.beatthebox.ui.slot_selection

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.btb.beatthebox.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BookingSummaryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val tvMessage: TextView
    private val content: View

    private val tvDate: TextView
    private val tvTime: TextView
    private val tvSport: TextView
    private val tvBox: TextView
    private val tvTotal: TextView

    init {
        inflate(context, R.layout.view_booking_summary, this)

        tvMessage = findViewById(R.id.tvMessage)
        content = findViewById(R.id.contentLayout)

        tvDate = findViewById(R.id.tvDate)
        tvTime = findViewById(R.id.tvTime)
        tvSport = findViewById(R.id.chipSport)
        tvBox = findViewById(R.id.chipBox)
        tvTotal = findViewById(R.id.tvTotal)
    }

    fun showMessage(message: String) {
        tvMessage.text = message
        tvMessage.visibility = View.VISIBLE
        content.visibility = View.GONE
    }

    fun showSummary(summary: BookingSummary) {
        tvDate.text = formatSelectedDate(summary.date)
        tvTime.text = summary.timeLabel()
        tvSport.text = summary.sport
        tvBox.text = summary.box
        tvTotal.text = "₹ ${summary.totalCost.toInt()}"

        tvMessage.visibility = View.GONE
        content.visibility = View.VISIBLE
    }

    fun BookingSummary.timeLabel(): String {
        fun fmt(min: Int) =
            java.time.LocalTime.of((min / 60)%24, min % 60)
                .format(DateTimeFormatter.ofPattern("h:mm a"))

        return "${fmt(unitToMinutes(startTime))} – ${fmt(unitToMinutes(endTime))}"
    }

    private fun formatSelectedDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, EEEE")
        return date.format(formatter)
    }

    fun unitToMinutes(unit: Int): Int {
        return (unit + 12) * 30
    }
}
