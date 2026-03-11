package com.btb.beatthebox.ui.reschedule

sealed class RescheduleActionState {
    object Idle : RescheduleActionState()
    object Loading : RescheduleActionState()
    data class Success(val newBookingId: String) : RescheduleActionState()
    data class Error(val message: String) : RescheduleActionState()
}
