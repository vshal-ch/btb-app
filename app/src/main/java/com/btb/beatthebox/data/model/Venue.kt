package com.btb.beatthebox.data.model

import com.google.gson.annotations.SerializedName

data class Venue(
    val name: String,
    val address: String,
    @SerializedName("gmaps_url") val gmapsUrl: String,
    val dimensions: String,
    val timings: String,
    @SerializedName("is_open") val isOpen: Boolean,
    val media: List<MediaItem>,
    val costs: List<TimeRangeCost>
)