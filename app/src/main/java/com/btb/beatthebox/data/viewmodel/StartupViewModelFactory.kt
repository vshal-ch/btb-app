package com.btb.beatthebox.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.btb.beatthebox.auth.AuthRepository
import com.btb.beatthebox.data.state.UserSession
import com.google.android.play.core.appupdate.AppUpdateManager

class StartupViewModelFactory(
    private val authRepository: AuthRepository,
    private val appUpdateManager: AppUpdateManager,
    private val userSession: UserSession,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StartupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StartupViewModel(
                authRepository = authRepository,
                appUpdateManager = appUpdateManager,
                userSession = userSession,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
