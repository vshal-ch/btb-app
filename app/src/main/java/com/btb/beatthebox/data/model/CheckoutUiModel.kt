package com.btb.beatthebox.data.model

data class CheckoutUiModel(
    val date: String,
    val startTime: Int,
    val endTime: Int,
    val sport: String,
    val box: String,
    val breakdown: List<PriceItem>,
    val total: Double,
    val policies: List<Note>
)