package com.btb.beatthebox.data.model

sealed class StartupState {
    object Loading : StartupState()
    object UpdateRequired : StartupState()
    object GoToLogin : StartupState()
    object GoToPhoneInput : StartupState()
    object GoToHome : StartupState()
    data class Error(val message: String) : StartupState()
}