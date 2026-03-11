package com.btb.beatthebox.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GamePass(
    val id: String,
    val box: String,
    val date: String,
    @SerializedName("start_time") val startTime: Int,
    @SerializedName("end_time") val endTime: Int,
    val sport: String,
    @SerializedName("game_pass_code") val gamePassCode: String,
    val attended: Boolean,
    @SerializedName("total_amount") val totalAmount: Double,
    @SerializedName("rescheduled_from_id") val rescheduledFromId: String?
) : Parcelable
