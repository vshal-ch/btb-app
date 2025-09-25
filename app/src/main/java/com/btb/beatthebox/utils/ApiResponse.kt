package com.btb.beatthebox.utils

import com.google.gson.annotations.SerializedName

// This is the raw response coming from backend
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("error") val error: String?,
    @SerializedName("data") val data: T?
)