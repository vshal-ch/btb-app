package com.btb.beatthebox.ui.slot_selection

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btb.beatthebox.constants.ALLOWABLE_WINDOW_MIN
import com.btb.beatthebox.data.model.AllSportDetail
import com.btb.beatthebox.data.model.CheckoutRequest
import com.btb.beatthebox.data.model.Note
import com.btb.beatthebox.data.model.Slot
import com.btb.beatthebox.di.ServiceLocator.bookingRepository
import com.btb.beatthebox.di.ServiceLocator.costRepository
import com.btb.beatthebox.ui.reschedule.SummaryState
import kotlinx.coroutines.launch
import java.time.LocalDate

class SlotSelectionViewModel : ViewModel() {

    private val _summary = MutableLiveData<SummaryState?>()
    val summary: LiveData<SummaryState?> = _summary


    private val _notesLive = MutableLiveData<List<Note>?>()
    val notesLive: LiveData<List<Note>?> = _notesLive

    private val _uiState =
        MutableLiveData<SlotSelectionState>()
    val uiState: LiveData<SlotSelectionState> = _uiState

    private var allSports: List<AllSportDetail> = emptyList()
    private var notes: List<Note> = emptyList()
    private var currentSlots: List<Slot> = emptyList()
    private var offerText: String = ""
    private var selectedDate: LocalDate = LocalDate.now()
    private var selectedSport: AllSportDetail? = null
    private var hasLoadedInitially = false

    private var selectedStartTimeUnit: Int? = null
    private var selectedEndTimeUnit: Int? = null

    init {
        loadInitialData()
    }

    // ---------------- COST ----------------

    private fun loadInitialData() {
        if (hasLoadedInitially) return
        hasLoadedInitially = true
        _uiState.value = SlotSelectionState.Loading

        viewModelScope.launch {
            try {
                // 1️⃣ Load cost + sport data
                allSports = costRepository.getAllSportDetails()

                if (allSports.isEmpty()) {
                    _uiState.value =
                        SlotSelectionState.Error("No sports available")
                    return@launch
                }

                // default sport → first one
                selectedSport = allSports.first()

                // 2️⃣ Load slots for today + default sport box
                val slots = bookingRepository.getSlots(
                    date = selectedDate.toString(),
                    box = selectedSport!!.box
                )
                currentSlots = slots

                offerText = costRepository.getOfferText()

                _uiState.value =
                    SlotSelectionState.Success(
                        sportDetails = allSports,
                        slots = slots,
                        offerText = offerText
                    )

                val noteResponse = bookingRepository.getSlotSelectionNotes()
                _notesLive.value = noteResponse
                if (noteResponse != null) {
                    notes = noteResponse
                }
            } catch (e: Exception) {
                _uiState.value =
                    SlotSelectionState.Error(
                        e.message ?: "Failed to load booking data"
                    )
            }
        }
    }

    // ---------------- USER ACTIONS ----------------

    fun onDateSelected(date: LocalDate) {
        selectedDate = date
        resetSummary()
        fetchSlots(false)
    }

    fun onSportSelected(sport: AllSportDetail) {
        if (sport.id == selectedSport?.id) return
        selectedSport = sport
        resetSummary()
        fetchSlots(false)
    }

    fun refresh() {
        fetchSlots(isRefresh = true)
    }

    private fun fetchSlots(isRefresh: Boolean) {
        val sportDetail = selectedSport ?: return

        _uiState.value =
            if (isRefresh) SlotSelectionState.Refreshing else SlotSelectionState.Loading

        viewModelScope.launch {
            try {
                val slots = bookingRepository.getSlots(
                    date = selectedDate.toString(),
                    box = sportDetail.box
                )
                currentSlots = slots

                offerText = costRepository.getOfferText()

                _uiState.value =
                    SlotSelectionState.Success(
                        sportDetails = allSports,
                        slots = slots,
                        offerText
                    )
            } catch (e: Exception) {
                _uiState.value =
                    SlotSelectionState.Error(
                        e.message ?: "Failed to load slots"
                    )
            }
        }
    }

    // ---------------- TIME & DURATION ----------------

    fun onSlotSelected(startUnit: Int, endUnit: Int) {
        selectedStartTimeUnit = startUnit
        selectedEndTimeUnit = endUnit
        val error = validateSlot(startUnit, endUnit)
        if (error != null) {
            _summary.value = SummaryState.SummaryError(error)
            return
        }

        calculateSummary(startUnit, endUnit)
    }

    private fun validateSlot(start: Int, end: Int): String? {
        // past time check
        if (selectedDate.isEqual(LocalDate.now())) {
            val nowMinutes =
                java.time.LocalTime.now().hour * 60 +
                        java.time.LocalTime.now().minute
            if (unitToMinutes(start) < (nowMinutes - ALLOWABLE_WINDOW_MIN)) {
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
        val sport = selectedSport ?: return
        val cost = PriceCalculator.calculate(
            cost = sport,
            start = start,
            end = end,
            date = selectedDate
        )

        val summary = BookingSummary(
            date = selectedDate,
            sport = sport.sport,
            startTime = start,
            endTime = end,
            duration = end - start,
            totalCost = cost,
            box = sport.box
        )

        var warning: String? = null
        if (selectedDate.isEqual(LocalDate.now())) {

            val nowMinutes =
                java.time.LocalTime.now().hour * 60 +
                        java.time.LocalTime.now().minute
            if (nowMinutes > unitToMinutes(start) && nowMinutes < unitToMinutes(start) + ALLOWABLE_WINDOW_MIN) {
                warning =
                    "You can book the slot within $ALLOWABLE_WINDOW_MIN minutes of the start time but you cannot play after the end time."
            }
        }

        _summary.value = SummaryState.Summary(
            summary, warning, null
        )
    }

    private fun resetSummary() {
        selectedStartTimeUnit = null
        selectedEndTimeUnit = null
        _summary.value = null
    }

    fun getCheckoutRequest(): CheckoutRequest? {
        val sport = selectedSport ?: return null
        val start = selectedStartTimeUnit ?: return null
        val end = selectedEndTimeUnit ?: return null

        return CheckoutRequest(
            date = selectedDate.toString(),//ISO Format
            startTime = start,
            endTime = end,
            sportId = sport.id,
            sportName = sport.sport,
            box = sport.box
        )
    }

    fun getSelectedDate(): LocalDate {
        return selectedDate
    }

    fun getSelectedSport(): AllSportDetail? {
        return selectedSport
    }

    fun unitToMinutes(unit: Int): Int {
        return (unit + 12) * 30
    }
}
