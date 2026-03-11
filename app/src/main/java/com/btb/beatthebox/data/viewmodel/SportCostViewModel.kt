package com.btb.beatthebox.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btb.beatthebox.data.model.AllSportDetail
import com.btb.beatthebox.data.model.SportCostDetail
import com.btb.beatthebox.di.ServiceLocator.costRepository
import com.btb.beatthebox.utils.ApiResult
import kotlinx.coroutines.launch

class SportCostViewModel(
) : ViewModel() {

    private val _cost =
        MutableLiveData<ApiResult<AllSportDetail>>()
    val cost: LiveData<ApiResult<AllSportDetail>> = _cost

    fun loadCost(sportId: String) {
        viewModelScope.launch {
            _cost.value = ApiResult.Loading
            try {
                val data = costRepository.getCostBySport(sportId)
                _cost.value = ApiResult.Success(data)
            } catch (e: Exception) {
                _cost.value =
                    ApiResult.Error(e.message ?: "Failed to load cost")
            }
        }
    }
}
