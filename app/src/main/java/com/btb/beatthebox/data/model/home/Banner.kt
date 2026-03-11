package com.btb.beatthebox.data.model.home

import com.google.gson.annotations.SerializedName

data class Banner(
    val id: String,
    @SerializedName("src_url") val srcUrl: String,
    val title: String,
    val description: String
)