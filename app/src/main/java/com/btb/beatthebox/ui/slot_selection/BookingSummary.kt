package com.btb.beatthebox.ui.slot_selection

import com.btb.beatthebox.data.model.Sport
import java.time.LocalDate

data class BookingSummary(
    val date: LocalDate = LocalDate.now(),
    val sport: String = "",
    val startTime: Int = 0,
    val endTime: Int = 2,
    val duration: Int = 0,        // in minutes
    val totalCost: Double = 0.0,
    val box: String = "",
)
