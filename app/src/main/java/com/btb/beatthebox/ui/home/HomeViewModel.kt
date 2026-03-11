package com.btb.beatthebox.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.btb.beatthebox.data.model.Venue
import com.btb.beatthebox.data.model.home.HomeResponse
import com.btb.beatthebox.data.model.home.UpcomingGame
import com.btb.beatthebox.data.remote.ApiClient
import com.btb.beatthebox.di.ServiceLocator.apiService
import com.btb.beatthebox.di.ServiceLocator.bookingRepository
import com.btb.beatthebox.utils.ApiResult
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _homeDetails = MutableLiveData<ApiResult<HomeResponse>>()
    val homeDetails: LiveData<ApiResult<HomeResponse>> get() = _homeDetails

    private var hasLoadedHome = false

    private val _upcomingGame = MutableLiveData<ApiResult<UpcomingGame?>>()
    val upcomingGame: LiveData<ApiResult<UpcomingGame?>> get() = _upcomingGame

    private var hasLoadedUpcomingGame = false

    fun fetchHomeDetailsIfNeeded() {
        if (hasLoadedHome) return
        loadHomeDetails(isRefresh = false)
    }

    fun refreshHomeDetails() {
        loadHomeDetails(isRefresh = true)
    }

    private fun loadHomeDetails(isRefresh: Boolean) {
        if (!isRefresh) {
            hasLoadedHome = true
        }

        _homeDetails.value =
            if (isRefresh) ApiResult.Refreshing else ApiResult.Loading

        viewModelScope.launch {
            try {
                val response = apiService.getHome()
                if (response.isSuccessful && response.body()?.success == true) {
                    _homeDetails.value =
                        ApiResult.Success(response.body()!!.data!!)
                } else {
                    hasLoadedHome = false
                    _homeDetails.value =
                        ApiResult.Error(response.body()?.error ?: "Unknown error")
                }
            } catch (e: Exception) {
                hasLoadedHome = false
                _homeDetails.value =
                    ApiResult.Error(e.localizedMessage ?: "Network error")
            }
        }
    }


    fun fetchUpcomingGameIfNeeded() {
        if (hasLoadedUpcomingGame) return
        loadUpcomingGame(isRefresh = false)
    }

    fun refreshUpcomingGame() {
        loadUpcomingGame(isRefresh = true)
    }

    private fun loadUpcomingGame(isRefresh: Boolean) {
        if (!isRefresh) {
            hasLoadedUpcomingGame = true
        }

        _upcomingGame.value =
            if (isRefresh) ApiResult.Refreshing else ApiResult.Loading

        viewModelScope.launch {
            try {
                val response = bookingRepository.getNextBookingForUser()
                _upcomingGame.value = ApiResult.Success(response)
            } catch (e: Exception) {
                hasLoadedUpcomingGame = false
                _upcomingGame.value =
                    ApiResult.Error(e.localizedMessage ?: "Something went wrong")
            }
        }
    }
}
