package com.btb.beatthebox.data.repository

import com.btb.beatthebox.data.model.CheckoutRequest
import com.btb.beatthebox.data.model.ConfirmedBookingResponse
import com.btb.beatthebox.data.model.Note
import com.btb.beatthebox.data.model.RescheduleRequest
import com.btb.beatthebox.data.model.Slot
import com.btb.beatthebox.data.model.SlotBookingResponse
import com.btb.beatthebox.data.model.UserGamesResponse
import com.btb.beatthebox.data.model.home.UpcomingGame
import com.btb.beatthebox.data.remote.ApiService
import com.btb.beatthebox.data.model.CheckoutUiModel
import com.btb.beatthebox.utils.errorMessage

class BookingRepository(
    private val api: ApiService
) {
    suspend fun getNextBookingForUser(): UpcomingGame? {
        val response = api.getNextBookingForUser()
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!

            if (body.success) {
                return body.data
            } else {
                throw IllegalStateException(
                    body.error ?: "Failed to fetch booking details"
                )
            }
        } else {
            throw IllegalStateException(
                "Booking API failed: ${response.errorMessage()}"
            )
        }
    }

    suspend fun getBookingById(bookingId: String): ConfirmedBookingResponse {
        val response = api.getBookingById(bookingId)
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!

            if (body.success && body.data != null) {
                return body.data
            } else {
                throw IllegalStateException(
                    body.error ?: "Failed to fetch booking details"
                )
            }
        } else {
            throw IllegalStateException(
                "Booking API failed: ${response.errorMessage()}"
            )
        }
    }

    suspend fun getSlots(date: String, box: String): List<Slot> {
        val response = api.getSlots(box, date)
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            if (body.success && body.data != null) {
                return body.data
            } else {
                throw IllegalStateException(
                    body.error ?: "Failed to fetch Slots"
                )
            }
        } else {
            throw IllegalStateException(
                "Slots API failed: ${response.errorMessage()}"
            )
        }
    }

    suspend fun getUserBookings(
        filter: String,
        page: Int
    ): UserGamesResponse {
        val response = api.getUserBookings(filter,page)
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            if (body.success && body.data != null) {
                return body.data
            } else {
                throw IllegalStateException(
                    body.error ?: "Failed to load games"
                )
            }
        } else {
            throw IllegalStateException(
                "Slots API failed: ${response.errorMessage()}"
            )
        }
    }

    suspend fun getSlotSelectionNotes(): List<Note>? {
        val response = api.getSlotSelectionNotes()
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            if (body.success && body.data != null) {
                return body.data
            }
        }
        return null
    }

    suspend fun getReschedulePolicies(): List<Note>? {
        val response = api.getReschedulePolicies()
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            if (body.success && body.data != null) {
                return body.data
            }
        }
        return null
    }

    suspend fun verifySlot(checkoutRequest: CheckoutRequest): CheckoutUiModel {
        val response = api.verifySlot(checkoutRequest)
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            if (body.success && body.data != null) {
                return body.data
            } else {
                throw IllegalStateException(
                    body.error ?: "Failed to validate slot"
                )
            }
        } else {
            throw IllegalStateException(
                "Slots API failed: ${response.errorMessage()}"
            )
        }
    }

    suspend fun bookSlot(checkoutRequest: CheckoutRequest): SlotBookingResponse {
        val response = api.bookSlot(checkoutRequest)
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            if (body.success && body.data != null) {
                return body.data
            } else {
                throw IllegalStateException(
                    body.error ?: "Failed to book slot"
                )
            }
        } else {
            throw IllegalStateException(
                "Slots API failed: ${response.errorMessage()}"
            )
        }
    }

    suspend fun rescheduleSlot(rescheduleRequest: RescheduleRequest): String{
        val response = api.rescheduleSlot(rescheduleRequest)
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            if (body.success && body.data != null) {
                return body.data
            } else {
                throw IllegalStateException(
                    body.error ?: "Failed to book slot"
                )
            }
        } else {
            throw IllegalStateException(
                "Slots API failed: ${response.errorMessage()}"
            )
        }
    }
}
