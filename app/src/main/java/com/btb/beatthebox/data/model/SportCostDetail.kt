package com.btb.beatthebox.data.model

import com.google.gson.annotations.SerializedName

data class SportCostDetail(
    val id: String,
    @SerializedName("sport_id") val sportId: String,
    val cost: Double,
    @SerializedName("evening_cost") val eveningCost: Int,
    @SerializedName("weekend_cost") val weekendCost: Int
)