package com.btb.beatthebox.ui.payments

import com.btb.beatthebox.data.model.PaymentVerifyResponse

sealed class VerifyState {
    object Verifying : VerifyState()
    class Success(
        val verifyResponse: PaymentVerifyResponse
    ) : VerifyState()
    object Failed : VerifyState()
    object Timeout : VerifyState()
}
