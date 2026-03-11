package com.btb.beatthebox.data.model

import com.google.gson.annotations.SerializedName

data class SlotBookingResponse(
    @SerializedName("booking_id") val bookingId: String,
    @SerializedName("internal_order_id") val internalOrderId: String,
    @SerializedName("gateway_order_id") val orderId: String,
    @SerializedName("payment_session_id") val paymentSessionId: String,
    @SerializedName("expires_at") val expiresAt: String
)