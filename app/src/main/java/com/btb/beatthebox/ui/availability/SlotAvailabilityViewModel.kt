package com.btb.beatthebox.ui.availability

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btb.beatthebox.data.model.Slot
import com.btb.beatthebox.data.remote.ApiClient
import com.btb.beatthebox.utils.ApiResult
import kotlinx.coroutines.launch

class SlotAvailabilityViewModel : ViewModel() {

    private val _slots = MutableLiveData<ApiResult<List<Slot>>>()
    val slots: LiveData<ApiResult<List<Slot>>> = _slots

    fun fetchSlots(date: String, currentTime: Int) {
        _slots.value = ApiResult.Loading
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getSlots(currentTime, date)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success && body.data != null) {
                        _slots.value = ApiResult.Success(body.data)
                    } else {
                        _slots.value =
                            ApiResult.Error(body.error ?: "Unknown error from server")
                    }
                } else {
                    _slots.value = ApiResult.Error("Failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _slots.value = ApiResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}
