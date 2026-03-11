package com.btb.beatthebox.data.model


data class ConfirmedBookingResponse(
    val venue: VenueAddressInfo,
    val booking: GamePass,
    val policies: List<Note>
)
