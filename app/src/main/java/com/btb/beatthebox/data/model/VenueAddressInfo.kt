package com.btb.beatthebox.data.model

import com.google.gson.annotations.SerializedName

data class VenueAddressInfo(
    val name: String,
    val address: String,
    @SerializedName("gmaps_url") var gmapsUrl: String
)