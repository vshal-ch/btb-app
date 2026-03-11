package com.btb.beatthebox.data.model

data class RescheduleRequest(
    val bookingId: String, val date: String,
    val startTime: Int,
    val endTime: Int
)