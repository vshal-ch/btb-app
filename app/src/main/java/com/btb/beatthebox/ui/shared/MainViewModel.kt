package com.btb.beatthebox.ui.shared

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.btb.beatthebox.data.model.Venue
import com.btb.beatthebox.data.remote.ApiClient
import com.btb.beatthebox.di.ServiceLocator.apiService
import com.btb.beatthebox.utils.ApiResult
import com.btb.beatthebox.utils.errorMessage
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _venueDetails = MutableLiveData<ApiResult<Venue>>()
    val venueDetails: LiveData<ApiResult<Venue>> get() = _venueDetails
    private var hasLoadedVenue = false

    fun fetchVenueDetailsIfNeeded() {
        if (hasLoadedVenue) return
        fetchVenueDetails(false)
    }

    fun refreshVenueDetails() {
        fetchVenueDetails(true)
    }

    fun fetchVenueDetails(isRefresh: Boolean) {
        if (!isRefresh) {
            hasLoadedVenue = true
        }
        _venueDetails.value = if (isRefresh) ApiResult.Refreshing else ApiResult.Loading
        viewModelScope.launch {
            try {
                val response = apiService.getVenue()
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success && body.data != null) {

                        _venueDetails.value = ApiResult.Success(body.data)
                    } else {
                        _venueDetails.value =
                            ApiResult.Error(body.error ?: "Unknown error from server")
                    }
                } else {
                    _venueDetails.value =
                        ApiResult.Error("Failed to load venue: ${response.errorMessage()}")
                }
            } catch (e: Exception) {
                _venueDetails.value = ApiResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}