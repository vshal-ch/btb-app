package com.btb.beatthebox.ui.checkout

import com.btb.beatthebox.data.model.CheckoutUiModel
import com.btb.beatthebox.data.model.SlotBookingResponse

sealed class CheckoutState {
    object Loading : CheckoutState()
    data class SlotVerified(val data: CheckoutUiModel) : CheckoutState()
    data class BookingCreated(
        val data: SlotBookingResponse
    ) : CheckoutState()

    data class VerifyError(val message: String) : CheckoutState()
    data class BookingError(val message: String) : CheckoutState()
}
