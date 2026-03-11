package com.btb.beatthebox.data.model

import com.google.gson.annotations.SerializedName

data class GameItem(
    val id: String,
    val sport: String,
    val box: String,
    val date: String,
    @SerializedName("start_time") val startTime: Int,
    @SerializedName("end_time") val endTime: Int,
    @SerializedName("total_amount") val totalAmount: Double,
)