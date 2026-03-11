package com.btb.beatthebox.data.model

import com.google.gson.annotations.SerializedName

data class Pagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    @SerializedName("has_next") val hasNext: Boolean
)