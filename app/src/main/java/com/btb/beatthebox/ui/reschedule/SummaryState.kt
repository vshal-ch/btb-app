package com.btb.beatthebox.ui.reschedule

import com.btb.beatthebox.ui.slot_selection.BookingSummary

sealed class SummaryState {
    data class SummaryError(val message: String) : SummaryState()
    data class Summary(val summary: BookingSummary, val warning: String?, val error: String?) :
        SummaryState()
}