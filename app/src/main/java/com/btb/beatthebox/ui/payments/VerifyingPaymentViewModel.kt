package com.btb.beatthebox.ui.payments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btb.beatthebox.data.repository.PaymentRepository
import com.btb.beatthebox.di.ServiceLocator.paymentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VerifyingPaymentViewModel : ViewModel() {

    private val _state = MutableLiveData<VerifyState>(VerifyState.Verifying)
    val state: LiveData<VerifyState> = _state

    private var verificationJob: Job? = null

    fun startVerification(orderId: String) {
        // Prevent duplicate polling
        if (verificationJob != null) return

        verificationJob = viewModelScope.launch {
            val maxAttempts = 10          // 10 × 2s = 20s
            val delayMillis = 2000L

            repeat(maxAttempts) {
                try {
                    val response =
                        paymentRepository.verifyPayment(orderId)

                    if (response != null) {
                        when {
                            response.paymentStatus == "SUCCESS" &&
                                    response.bookingStatus == "CONFIRMED" -> {
                                _state.postValue(VerifyState.Success(response))
                                return@launch
                            }

                            response.paymentStatus == "FAILED" ||
                                    response.bookingStatus == "EXPIRED" -> {
                                _state.postValue(VerifyState.Failed)
                                return@launch
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Network error — keep retrying until timeout
                }

                delay(delayMillis)
            }

            _state.postValue(VerifyState.Timeout)
        }
    }

    override fun onCleared() {
        verificationJob?.cancel()
        super.onCleared()
    }
}
