package com.btb.beatthebox.data.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btb.beatthebox.data.model.User
import com.btb.beatthebox.data.state.UserSession
import com.btb.beatthebox.di.ServiceLocator.authRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel: ViewModel() {
    val user: StateFlow<User?> = UserSession.user

    fun setUser(user: User) {
        UserSession.setUser(user)
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.logout()
            } finally {
                UserSession.clear()
                onComplete()
            }
        }
    }


    fun clear() {
        UserSession.clear()
    }
}