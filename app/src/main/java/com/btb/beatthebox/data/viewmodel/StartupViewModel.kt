package com.btb.beatthebox.data.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btb.beatthebox.auth.AuthRepository
import com.btb.beatthebox.data.model.StartupState
import com.btb.beatthebox.data.state.UserSession
import com.btb.beatthebox.di.ServiceLocator.apiService
import com.btb.beatthebox.utils.errorMessage
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class StartupViewModel(
    private val authRepository: AuthRepository,
    private val appUpdateManager: AppUpdateManager,
    private val userSession: UserSession,
) : ViewModel() {

    private val _state = MutableStateFlow<StartupState>(StartupState.Loading)
    val state: StateFlow<StartupState> = _state

    private var updateListenerRegistered = false

    init {
        resolveStartup()
    }

    private fun resolveStartup() {
        viewModelScope.launch {
            // Step 1: check updates first
            val updateInfo = try {
                appUpdateManager.appUpdateInfo.await()
            } catch (e: Exception) {
                // IMPORTANT: ignore update errors (e.g. app not from Play Store)
                null
            }

            if (updateInfo != null) {
                when {
                    updateInfo.updateAvailability() ==
                            UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                        _state.value = StartupState.UpdateRequired
                        return@launch
                    }

                    updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                            updateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> {
                        _state.value = StartupState.UpdateRequired
                        return@launch
                    }
                }
            }


            val token = authRepository.getAccessToken()

            if (token.isNullOrEmpty()) {
                _state.value = StartupState.GoToLogin
                return@launch
            }

            try {
                val response = apiService.validateToken("Bearer $token")
                Log.d("DEBUG", "Response: $response")
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    if (body.success && body.data != null) {
                        Log.d("DEBUG", "Success: ${body}")
                        val user = body.data

                        // Store user globally
                        userSession.setUser(user)
                        Log.d("DEBUG", "User: $user")

                        // Decide navigation
                        _state.value =
                            if (user.newUser) {
                                StartupState.GoToPhoneInput
                            } else {
                                StartupState.GoToHome
                            }

                    } else {
                        // Backend responded but logical failure
                        _state.value = StartupState.Error(
                            body.error ?: "Unknown error from server"
                        )
                    }

                } else {
                    // HTTP error (401, 500, etc.)
                    if (response.code() == 401) {
                        // Refresh may already have been attempted by Authenticator
//                        authRepository.logout()
                        _state.value = StartupState.GoToLogin
                    } else {
                        _state.value = StartupState.Error(
                            "Auth validation failed: ${response.errorMessage()}"
                        )
                    }
                }

            } catch (e: Exception) {
                _state.value = StartupState.Error(
                    "Unable to contact server, Please check your internet! ${e.localizedMessage}"
                )
            }
        }
    }

    fun registerUpdateListener(listener: InstallStateUpdatedListener) {
        if (!updateListenerRegistered) {
            appUpdateManager.registerListener(listener)
            updateListenerRegistered = true
        }
    }

    fun unregisterUpdateListener(listener: InstallStateUpdatedListener) {
        if (updateListenerRegistered) {
            appUpdateManager.unregisterListener(listener)
            updateListenerRegistered = false
        }
    }
}
