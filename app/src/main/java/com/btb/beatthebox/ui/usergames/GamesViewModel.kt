package com.btb.beatthebox.ui.usergames

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btb.beatthebox.data.model.GameItem
import com.btb.beatthebox.di.ServiceLocator.bookingRepository
import com.btb.beatthebox.utils.ApiResult
import kotlinx.coroutines.launch

class GamesViewModel : ViewModel() {

    private val _state = MutableLiveData<ApiResult<List<GameItem>>>()
    val state: LiveData<ApiResult<List<GameItem>>> = _state

    private var currentPage = 1
    private var currentFilter = "all"
    private var hasNext = true
    private val items = mutableListOf<GameItem>()

    private var hasLoadedOnce = false
    private var lastFilter: String? = null

    fun load(filter: String, reset: Boolean = false) {
        if (filter == lastFilter && hasLoadedOnce && !reset) return
        lastFilter = filter
        hasLoadedOnce = true
        if (reset) {
            currentPage = 1
            hasNext = true
            items.clear()
            currentFilter = filter
        }

        if (!hasNext) return

        _state.value = ApiResult.Loading

        viewModelScope.launch {
            try {
                val res = bookingRepository.getUserBookings(filter, currentPage)

                items.addAll(res.items)
                hasNext = res.pagination.hasNext
                currentPage++

                _state.value = ApiResult.Success(items.toList())
            } catch (e: Exception) {
                _state.value = ApiResult.Error(e.message ?: "Failed to load games");
            }
        }
    }
}
