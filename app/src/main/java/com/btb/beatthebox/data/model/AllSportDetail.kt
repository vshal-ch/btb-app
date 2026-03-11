package com.btb.beatthebox.data.model

import com.google.gson.annotations.SerializedName

data class AllSportDetail(
    val id: String,
    val cost: Double,
    @SerializedName("evening_cost") val eveningCost: Int,
    @SerializedName("weekend_cost") val weekendCost: Int,
    val sport: String,
    val box: String
)