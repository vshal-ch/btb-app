package com.btb.beatthebox.data.state

sealed class LoginState {
    object Idle : LoginState()          // default / no-op
    object Loading : LoginState()
    data class Error(val message: String?) : LoginState()
}
