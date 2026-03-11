package com.btb.beatthebox.ui.faq

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.btb.beatthebox.data.model.Faq
import com.btb.beatthebox.data.repository.FaqRepository
import com.btb.beatthebox.utils.ApiResult
import kotlinx.coroutines.launch

class FaqViewModel(
    private val repository: FaqRepository
) : ViewModel(), ViewModelProvider.Factory {
    class Factory(private val faqRepository: FaqRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FaqViewModel(faqRepository) as T
        }
    }

    private val _state = MutableLiveData<ApiResult<List<Faq>>>()
    val state: LiveData<ApiResult<List<Faq>>> = _state

    fun loadFaqs() {
        _state.value = ApiResult.Loading

        viewModelScope.launch {
            try {
                val response = repository.getFaqs()
                _state.value = ApiResult.Success(response)
            } catch (e: Exception) {
                _state.value = ApiResult.Error(
                    e.message ?: "Something went wrong"
                )
            }
        }
    }
}
