package com.btb.beatthebox.ui.checkout

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btb.beatthebox.data.model.CheckoutRequest
import com.btb.beatthebox.di.ServiceLocator.bookingRepository
import kotlinx.coroutines.launch

class CheckoutViewModel : ViewModel() {

    private val _state = MutableLiveData<CheckoutState>()
    val state: LiveData<CheckoutState> = _state

    private var lastVerifiedRequest: CheckoutRequest? = null

    fun verifySlot(checkoutRequest: CheckoutRequest) {
        _state.value = CheckoutState.Loading

        viewModelScope.launch {
            try {
                val response = bookingRepository.verifySlot(checkoutRequest)
                lastVerifiedRequest = checkoutRequest
                _state.value = CheckoutState.SlotVerified(
                    response
                )
            } catch (e: Exception) {
                _state.value =
                    CheckoutState.VerifyError(e.message ?: "Validation failed")
            }
        }
    }

    fun onPayNow() {
        val request = lastVerifiedRequest
            ?: return _state.postValue(
                CheckoutState.VerifyError("Invalid booking state")
            )

        _state.value = CheckoutState.Loading

        viewModelScope.launch {
            try {
                val response = bookingRepository.bookSlot(request)
                _state.value = CheckoutState.BookingCreated(response)
            } catch (e: Exception) {
                _state.value =
                    CheckoutState.BookingError(e.message ?: "Unable to start payment")
            }
        }
    }
}
