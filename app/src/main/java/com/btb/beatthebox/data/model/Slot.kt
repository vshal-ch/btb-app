package com.btb.beatthebox.data.model

import com.google.gson.annotations.SerializedName

data class Slot(
    val id: String,
    @SerializedName("start_time") val startTime: Int,
    @SerializedName("end_time") val endTime: Int,
    val date: String,
    val sport: String
)