package com.btb.beatthebox.data.state

import android.util.Log
import com.btb.beatthebox.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow

object UserSession {
    val user = MutableStateFlow<User?>(null)

    fun setUser(user: User) {
        Log.d("UserSession", "Setting user: $user")
        this.user.value = user
    }

    fun clear() {
        user.value = null
    }
}