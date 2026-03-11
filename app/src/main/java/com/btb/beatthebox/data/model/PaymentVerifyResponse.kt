package com.btb.beatthebox.data.model

import com.google.gson.annotations.SerializedName

class PaymentVerifyResponse(
    @SerializedName("payment_status") val paymentStatus: String,
    @SerializedName("booking_status") val bookingStatus: String,
    @SerializedName("booking_id") val bookingId: String,
)