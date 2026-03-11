package com.btb.beatthebox.data.repository

import com.btb.beatthebox.data.model.PaymentVerifyResponse
import com.btb.beatthebox.data.remote.ApiService
import com.btb.beatthebox.utils.errorMessage

class PaymentRepository(
    private val api: ApiService
) {

    suspend fun verifyPayment(orderId: String): PaymentVerifyResponse? {
        val response = api.verifyPayment(orderId)
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!

            if (body.success) {
                return body.data
            } else {
                throw IllegalStateException(
                    body.error ?: "Failed to payment"
                )
            }
        } else {
            throw IllegalStateException(
                "Payments API failed: ${response.errorMessage()}"
            )
        }
    }
}