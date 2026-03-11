package com.btb.beatthebox.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CheckoutRequest(
    val date: String,        // yyyy-MM-dd
    val startTime: Int,      // minutes from midnight
    val endTime: Int,
    val sportId: String,
    val sportName: String,
    val box: String
) : Parcelable
