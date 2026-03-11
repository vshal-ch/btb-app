package com.btb.beatthebox.ui.gamepass

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btb.beatthebox.data.model.ConfirmedBookingResponse
import com.btb.beatthebox.di.ServiceLocator.bookingRepository
import com.btb.beatthebox.utils.ApiResult
import kotlinx.coroutines.launch

class GamePassViewModel(
) : ViewModel() {

    private val _state = MutableLiveData<ApiResult<ConfirmedBookingResponse>>()
    val state: LiveData<ApiResult<ConfirmedBookingResponse>> = _state

    private var lastBookingId: String? = null

    fun loadBooking(bookingId: String) {
        if (lastBookingId == bookingId) {
            return
        }
        lastBookingId = bookingId

        _state.value = ApiResult.Loading

        viewModelScope.launch {
            try {
                val booking = bookingRepository.getBookingById(bookingId)
                _state.value = ApiResult.Success(booking)
            } catch (e: Exception) {
                _state.value = ApiResult.Error(
                    e.message ?: "Unable to load booking"
                )
            }
        }
    }
}
