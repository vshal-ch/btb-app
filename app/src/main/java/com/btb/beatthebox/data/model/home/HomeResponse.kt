package com.btb.beatthebox.data.model.home

import com.google.gson.annotations.SerializedName

data class HomeResponse(
    val name: String,
    @SerializedName("is_open") val isOpen: Boolean,
    val timings: String,
    val banners: List<Banner>,
    val amenities: List<Amenity>
)