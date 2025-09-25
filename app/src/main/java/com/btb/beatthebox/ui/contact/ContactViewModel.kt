package com.btb.beatthebox.ui.contact

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btb.beatthebox.data.model.Contact
import com.btb.beatthebox.data.remote.ApiClient
import com.btb.beatthebox.utils.ApiResult
import kotlinx.coroutines.launch

class ContactViewModel : ViewModel() {

    private val _contacts = MutableLiveData<ApiResult<List<Contact>>>()
    val contacts: LiveData<ApiResult<List<Contact>>> = _contacts

    fun loadContacts() {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getContacts()
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success && body.data != null) {
                        _contacts.value = ApiResult.Success(body.data)
                    } else {
                        _contacts.value = ApiResult.Error(body.error ?: "Unknown error from server")
                    }
                } else {
                    _contacts.value = ApiResult.Error("Failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _contacts.value = ApiResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}
