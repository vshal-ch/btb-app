package com.btb.beatthebox.data.repository

import com.btb.beatthebox.data.model.Faq
import com.btb.beatthebox.data.remote.ApiService
import com.btb.beatthebox.utils.errorMessage

class FaqRepository(
    private val api: ApiService
) {
    suspend fun getFaqs(): List<Faq> {
        val response = api.getFaqs()
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!

            if (body.success && body.data != null) {
                return body.data
            } else {
                throw IllegalStateException(
                    body.error ?: "Failed to fetch FAQ"
                )
            }
        } else {
            throw IllegalStateException(
                "FAQ API failed: ${response.errorMessage()}"
            )
        }
    }
}
