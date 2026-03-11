package com.btb.beatthebox.data.model.home

import com.google.gson.annotations.SerializedName

data class UpcomingGame(
    val id: String,
    @SerializedName("start_time") val startTime: Int,
    @SerializedName("end_time") val endTime: Int,
    val date: String,
    val sport: String,
    val box: String
)