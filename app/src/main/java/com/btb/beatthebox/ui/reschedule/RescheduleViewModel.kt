package com.btb.beatthebox.ui.reschedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.btb.beatthebox.data.model.AllSportDetail
import com.btb.beatthebox.data.model.GamePass
import com.btb.beatthebox.data.model.Note
import com.btb.beatthebox.data.model.RescheduleRequest
import com.btb.beatthebox.data.model.Slot
import com.btb.beatthebox.di.ServiceLocator.bookingRepository
import com.btb.beatthebox.di.ServiceLocator.costRepository
import com.btb.beatthebox.ui.slot_selection.BookingSummary
import com.btb.beatthebox.ui.slot_selection.PriceCalculator
import com.btb.beatthebox.utils.ApiResult
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class RescheduleViewModel(private var from: GamePass) : ViewModel() {
    private val _rescheduleState =
        MutableLiveData<RescheduleActionState>(RescheduleActionState.Idle)
    val rescheduleState: LiveData<RescheduleActionState> = _rescheduleState

    private val _blockedSlots = MutableLiveData<ApiResult<List<Slot>>>()
    val blockedSlots: LiveData<ApiResult<List<Slot>>> = _blockedSlots

    private val _policyNotes = MutableLiveData<List<Note>?>()
    val policyNotes: LiveData<List<Note>?> = _policyNotes

    private val _toSummaryState = MutableLiveData<SummaryState?>()
    val toSummaryState: LiveData<SummaryState?> = _toSummaryState


    private var currentSlots: List<Slot> = emptyList()
    private var fromDate: LocalDate = LocalDate.parse(from.date, DateTimeFormatter.ISO_DATE)
    private var selectedDate: LocalDate = fromDate
    private var selectedBox: String = from.box
    private var selectedStartTimeUnit: Int = from.startTime
    private var selectedEndTimeUnit: Int = from.endTime
    private var costDetail: AllSportDetail? = null
    private var fromPriceWithoutDiscount: Double = from.totalAmount

    init {
        fetchBlockedSlots()
    }

    private fun fetchBlockedSlots() {
        _blockedSlots.value = ApiResult.Loading
        viewModelScope.launch {
            try {
                costDetail = costRepository.getCostBySport(from.sport)
                calculatePrice()
                val response =
                    bookingRepository.getSlots(from.date, from.box).filter { it.id != from.id }
                currentSlots = response
                _blockedSlots.value = ApiResult.Success(response)
                val policies = bookingRepository.getReschedulePolicies()
                _policyNotes.value = policies
            } catch (e: Exception) {
                _blockedSlots.value = ApiResult.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun fetchSlots() {
        _blockedSlots.value = ApiResult.Loading

        viewModelScope.launch {
            try {
                val slots = bookingRepository.getSlots(
                    date = selectedDate.toString(),
                    box = selectedBox
                ).filter { it.id != from.id }
                currentSlots = slots
                _blockedSlots.value = ApiResult.Success(slots)
            } catch (e: Exception) {
                _blockedSlots.value =
                    ApiResult.Error(
                        e.message ?: "Failed to load slots"
                    )
            }
        }
    }

    fun onDateSelected(date: LocalDate) {
        selectedDate = date
        resetSummary()
        fetchSlots()
    }

    fun onSlotSelected(startUnit: Int, endUnit: Int) {
        selectedStartTimeUnit = startUnit
        selectedEndTimeUnit = endUnit
        val error = validateSlot(startUnit, endUnit)
        if (error != null) {
            _toSummaryState.value = SummaryState.SummaryError(error)
            return
        }
        calculateSummary(startUnit, endUnit)
    }

    private fun validateSlot(start: Int, end: Int): String? {

        val bookingStart = fromDate.atTime(
            LocalTime.of(
                unitToMinutes(from.startTime) / 60,
                unitToMinutes(from.startTime) % 60
            )
        )

        if (LocalDateTime.now().isAfter(bookingStart.minusHours(2))) {
            return "Rescheduling allowed only up to 2 hours before start time"
        }

        // past time check
        if (selectedDate.isEqual(LocalDate.now())) {
            val nowMinutes =
                LocalTime.now().hour * 60 +
                        LocalTime.now().minute

            if (unitToMinutes(start) < nowMinutes) {
                return "Selected slot is in the past"
            }
        }

        // overlap check
        val overlaps = currentSlots.any { slot ->
            start < slot.endTime && end > slot.startTime
        }

        if (overlaps) {
            return "Selected slot overlaps with a booked slot"
        }


        return null
    }

    private fun calculateSummary(start: Int, end: Int) {
        val sport = costDetail ?: return
        val cost = PriceCalculator.calculate(
            cost = sport,
            start = start,
            end = end,
            date = selectedDate
        )
        val summary = BookingSummary(
            date = selectedDate,
            sport = from.sport,
            startTime = start,
            endTime = end,
            duration = end - start,
            totalCost = cost,
            box = from.box
        )
        if (fromDate == selectedDate && start == from.startTime && end == from.endTime) {
            _toSummaryState.value =
                SummaryState.Summary(summary, "Selected slot is same as original slot")
            return
        }

        if (end - start != from.endTime - from.startTime) {
            _toSummaryState.value =
                SummaryState.Summary(summary, "Selected slot duration is not same as original slot")
            return
        }
        if (cost != fromPriceWithoutDiscount) {
            _toSummaryState.value =
                SummaryState.Summary(summary, "Price should remain same")
            return
        }
        _toSummaryState.value = SummaryState.Summary(summary, null)
    }

    fun submitReschedule() {
        val start = selectedStartTimeUnit
        val end = selectedEndTimeUnit
        val date = selectedDate

        if (_toSummaryState.value !is SummaryState.Summary ||
            (_toSummaryState.value as SummaryState.Summary).error != null
        ) {
            return
        }

        _rescheduleState.value = RescheduleActionState.Loading

        viewModelScope.launch {
            try {
                val newBookingId = bookingRepository.rescheduleSlot(
                    RescheduleRequest(
                        bookingId = from.id,
                        date = date.toString(),
                        startTime = start,
                        endTime = end
                    )
                )

                _rescheduleState.value =
                    RescheduleActionState.Success(newBookingId)

            } catch (e: Exception) {
                _rescheduleState.value =
                    RescheduleActionState.Error(
                        e.message ?: "Failed to reschedule"
                    )
            }
        }
    }


    private fun calculatePrice() {
        if (costDetail == null) return;
        fromPriceWithoutDiscount = PriceCalculator.calculate(
            costDetail!!,
            from.startTime,
            from.endTime,
            fromDate
        )
    }

    private fun resetSummary() {
        selectedStartTimeUnit = from.startTime
        selectedEndTimeUnit = from.endTime
        _toSummaryState.value = null
    }

    fun getSelectedDate(): LocalDate {
        return selectedDate
    }

    fun getFromPriceWithoutDiscount(): Double {
        return fromPriceWithoutDiscount
    }

    fun unitToMinutes(unit: Int): Int {
        return (unit + 12) * 30
    }

    class RescheduleViewModelFactory(private val from: GamePass) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RescheduleViewModel::class.java)) {
                return RescheduleViewModel(from) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }

    }
}