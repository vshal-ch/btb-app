package com.btb.beatthebox.data.model

import com.google.gson.annotations.SerializedName

data class MediaItem(
    val id: String,
    val type: String,
    @SerializedName("src_url") val srcUrl: String,
    val description: String
)