package com.btb.beatthebox.ui.availability

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btb.beatthebox.data.model.Slot
import com.btb.beatthebox.data.remote.ApiClient
import com.btb.beatthebox.di.ServiceLocator.apiService
import com.btb.beatthebox.di.ServiceLocator.bookingRepository
import com.btb.beatthebox.utils.ApiResult
import kotlinx.coroutines.launch

class SlotAvailabilityViewModel : ViewModel() {

    private val _slots = MutableLiveData<ApiResult<List<Slot>>>()
    val slots: LiveData<ApiResult<List<Slot>>> = _slots
    private var hasLoadedSlots = false
    private var lastDate: String = "";
    private var lastBox: String = "";

    fun setLoaded(loaded: Boolean) {
        hasLoadedSlots = loaded
    }

    fun fetchSlotsIfNeeded(date: String, box: String) {
        if (hasLoadedSlots) return
        fetchSlots(date, box, false)
    }

    fun refreshSlots(date: String, box: String) {
        fetchSlots(date, box, true)
    }

    fun fetchSlots(date: String, box: String, isRefresh: Boolean) {
        if(date == lastDate && box == lastBox){
            return
        }
        if (!isRefresh) {
            hasLoadedSlots = true
        }

        _slots.value = if (isRefresh) ApiResult.Refreshing else ApiResult.Loading
        viewModelScope.launch {
            try {
                val response = bookingRepository.getSlots(date, box)
                lastBox = box
                lastDate = date
                _slots.value = ApiResult.Success(response)
            } catch (e: Exception) {
                _slots.value = ApiResult.Error(e.message ?: "Something went wrong")
            }
        }
    }
}
