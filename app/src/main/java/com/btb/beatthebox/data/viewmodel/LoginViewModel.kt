package com.btb.beatthebox.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.btb.beatthebox.auth.AuthRepository
import com.btb.beatthebox.data.state.LoginState
import com.btb.beatthebox.data.state.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val userSession: UserSession
) : ViewModel() {

    class Factory(
        private val authRepository: AuthRepository,
        private val userSession: UserSession
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(authRepository, userSession) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    fun onGoogleLoginSuccess(
        idToken: String,
    ) {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            try {
                val res = authRepository.loginWithGoogle(idToken)

                // 🔑 Single source of truth
                userSession.setUser(res.user)

                _state.value = LoginState.Idle
            } catch (e: Exception) {
                _state.value = LoginState.Error(
                    e.localizedMessage ?: "Login failed"
                )
            }
        }
    }

    fun submitProfile(name: String?, phone: String?) {

        viewModelScope.launch {
            _state.value = LoginState.Loading
            try {
                val updatedUser =
                    authRepository.completeProfile(name, phone)

                userSession.setUser(updatedUser)

                _state.value = LoginState.Idle
            } catch (e: Exception) {
                _state.value = LoginState.Error(
                    e.localizedMessage ?: "Profile update failed"
                )
            }
        }
    }
}
