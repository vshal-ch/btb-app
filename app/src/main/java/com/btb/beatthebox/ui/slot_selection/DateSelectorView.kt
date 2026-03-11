package com.btb.beatthebox.ui.slot_selection

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.btb.beatthebox.R
import com.btb.beatthebox.utils.ThemeUtils
import com.google.android.material.card.MaterialCardView
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class DateSelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    var onDateSelected: ((LocalDate) -> Unit)? = null

    private lateinit var container: LinearLayout
    private lateinit var cardToday: MaterialCardView
    private lateinit var cardTomorrow: MaterialCardView
    private lateinit var cardDayAfter: MaterialCardView
    private lateinit var calendarCard: MaterialCardView
    private lateinit var tvSelectedDate: TextView

    private var selectedDate: LocalDate = LocalDate.now()

    init {
        orientation = HORIZONTAL
        inflate(context, R.layout.view_date_selector, this)
        setupViews()
    }

    private fun setupViews() {
        container = findViewById(R.id.dateButtonContainer)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        container.removeAllViews()

        val today = LocalDate.now()

        cardToday = addDateItem(today, "Today")
        cardTomorrow = addDateItem(today.plusDays(1), "Tomorrow")
        cardDayAfter = addDateItem(today.plusDays(2), "Day after")
        calendarCard = addCalendarItem()

        selectInternal(today, cardToday)
    }

    private fun addDateItem(
        date: LocalDate,
        label: String
    ): MaterialCardView {

        val view = inflateItem()
        val card = view.findViewById<MaterialCardView>(R.id.card)

        val dayNumber = view.findViewById<TextView>(R.id.tvDayNumber)
        val dayName = view.findViewById<TextView>(R.id.tvDayName)
        val labelTv = view.findViewById<TextView>(R.id.tvLabel)

        view.findViewById<ImageView>(R.id.ivCalendar).visibility = View.GONE
        view.findViewById<View>(R.id.dateContent).visibility = View.VISIBLE

        dayNumber.text = date.dayOfMonth.toString()
        dayName.text = date.dayOfWeek
            .getDisplayName(TextStyle.SHORT, Locale.getDefault())
        labelTv.text = label

        card.setOnClickListener {
            selectInternal(date, card)
        }

        container.addView(view)
        return card
    }

    private fun addCalendarItem(): MaterialCardView {
        val view = inflateItem()
        val card = view.findViewById<MaterialCardView>(R.id.card)

        view.findViewById<View>(R.id.dateContent).visibility = View.GONE
        view.findViewById<ImageView>(R.id.ivCalendar).visibility = View.VISIBLE
        view.findViewById<TextView>(R.id.tvLabel).text = "Pick a date"

        card.setOnClickListener {
            openDatePicker()
        }

        container.addView(view)
        return card
    }

    private fun inflateItem(): View =
        LayoutInflater.from(context)
            .inflate(R.layout.view_date_selector_item, container, false)

    private fun selectInternal(date: LocalDate, card: MaterialCardView) {
        selectedDate = date
        resetAllCards()
        applySelectedStyle(card)
        tvSelectedDate.text = formatSelectedDate(date)
        onDateSelected?.invoke(date)
    }

    private fun resetAllCards() {
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            val card = child.findViewById<MaterialCardView>(R.id.card)

            card.setCardBackgroundColor(Color.TRANSPARENT)
            card.strokeWidth = 1
            card.strokeColor = ThemeUtils.getColor(
                context, R.attr.subTextColor
            )

            child.findViewById<TextView>(R.id.tvDayNumber)
                .setTextColor(
                    ThemeUtils.getColor(context, android.R.attr.textColor)
                )

            child.findViewById<TextView>(R.id.tvDayName)
                .setTextColor(
                    ThemeUtils.getColor(context, android.R.attr.textColor)
                )
        }
    }

    private fun applySelectedStyle(card: MaterialCardView) {
        card.setCardBackgroundColor(
            ThemeUtils.getColor(context, android.R.attr.colorPrimary)
        )
        card.strokeWidth = 0

        card.findViewById<TextView>(R.id.tvDayNumber)
            .setTextColor(Color.WHITE)
        card.findViewById<TextView>(R.id.tvDayName)
            .setTextColor(Color.WHITE)
    }

    private fun openDatePicker() {

        val todayUtc = MaterialDatePicker.todayInUtcMilliseconds()

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = todayUtc
        calendar.add(Calendar.DAY_OF_YEAR, 30)
        val thirtyDaysLater = calendar.timeInMillis

        val validators = listOf(
            DateValidatorPointForward.from(todayUtc),       // Minimum: Today
            DateValidatorPointBackward.before(thirtyDaysLater) // Maximum: 30 days from now
        )

        val constraints = CalendarConstraints.Builder()
            .setValidator(
                CompositeDateValidator.allOf(validators)
            )
            .setStart(todayUtc)
            .setEnd(thirtyDaysLater)
            .build()

        val picker = MaterialDatePicker.Builder.datePicker()
            .setCalendarConstraints(constraints)
            .setSelection(todayUtc)
            .build()

        picker.addOnPositiveButtonClickListener { millis ->
            val date = Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            when (date) {
                LocalDate.now() ->
                    selectInternal(date, cardToday)
                LocalDate.now().plusDays(1) ->
                    selectInternal(date, cardTomorrow)
                LocalDate.now().plusDays(2) ->
                    selectInternal(date, cardDayAfter)
                else -> {
                    updateCalendarCard(date)
                    selectInternal(date, calendarCard)
                }
            }
        }

        picker.show(
            (context as FragmentActivity).supportFragmentManager,
            "DATE_PICKER"
        )
    }

    private fun updateCalendarCard(date: LocalDate) {
        val icon = calendarCard.findViewById<ImageView>(R.id.ivCalendar)
        val content = calendarCard.findViewById<View>(R.id.dateContent)
        val day = calendarCard.findViewById<TextView>(R.id.tvDayNumber)
        val week = calendarCard.findViewById<TextView>(R.id.tvDayName)

        icon.visibility = View.GONE
        content.visibility = View.VISIBLE

        day.text = date.dayOfMonth.toString()
        week.text = date.dayOfWeek
            .getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }

    fun setDate(date: LocalDate){
        when {
            date == LocalDate.now() ->
                selectInternal(date, cardToday)

            date == LocalDate.now().plusDays(1) ->
                selectInternal(date, cardTomorrow)

            date == LocalDate.now().plusDays(2) ->
                selectInternal(date, cardDayAfter)

            else -> {
                updateCalendarCard(date)
                selectInternal(date, calendarCard)
            }
        }
    }

    companion object {
        fun formatSelectedDate(date: LocalDate): String {
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, EEEE")
            return date.format(formatter)
        }
    }
}
