package com.btb.beatthebox.auth

import android.util.Log
import com.btb.beatthebox.data.model.login.LogoutRequest
import com.btb.beatthebox.data.model.User
import com.btb.beatthebox.data.model.login.CompleteProfileRequest
import com.btb.beatthebox.data.model.login.GoogleLoginRequest
import com.btb.beatthebox.data.model.login.GoogleLoginResponse
import com.btb.beatthebox.data.remote.ApiService
import com.btb.beatthebox.utils.errorMessage

class AuthRepository(
    private val tokenStore: TokenStore, private val api: ApiService
) {

//    val authState: Flow<AuthState> = tokenStore.accessTokenFlow.flatMapLatest { token ->
//        if (token.isNullOrEmpty()) {
//            flowOf(AuthState.Unauthenticated)
//        } else {
//            validateOrRefresh(token)
//        }
//    }.onStart { emit(AuthState.Loading) }.distinctUntilChanged()
//
//    private fun validateOrRefresh(token: String): Flow<AuthState> = flow {
//        try {
//            val res = api.validateToken("Bearer $token")
//            if(res.body() == null){
//                throw HttpException("No body")
//            }
//            val userData = res.body()?.data
//            val user = User(
//                id = res.id,
//                name = res.name,
//                email = res.email,
//                phoneNo = res.phoneNo,
//                avatarUrl = res.avatarUrl,
//                newUser = res.newUser
//            )
//            emit(AuthState.Authenticated)
//        } catch (e: HttpException) {
//            emit(AuthState.Unauthenticated)
//        }
//    }

//    private suspend fun tryRefresh(): Boolean {
//        val refreshToken = tokenStore.getRefreshToken() ?: return false
//
//        return try {
//            val res = api.refreshToken(
//                RefreshTokenRequest(refreshToken)
//            )
//
//            tokenStore.saveTokens(
//                access = res.accessToken, refresh = res.refreshToken
//            )
//            true
//        } catch (e: Exception) {
//            false
//        }
//    }

    /* ---------------------------------------------------------
     * LOGIN & ONBOARDING (NEW)
     * --------------------------------------------------------- */
    suspend fun getAccessToken(): String? {
        return tokenStore.getAccessToken()
    }
    /**
     * Login with Google ID token.
     *
     * Backend decides:
     *  - existing user → tokens valid immediately
     *  - new user → requires profile completion
     */
    suspend fun loginWithGoogle(
        idToken: String
    ): GoogleLoginResponse {

        val response = api.loginWithGoogle(
            GoogleLoginRequest(idToken = idToken)
        )

        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!

            if (body.success && body.data != null) {
                val data = body.data

                // Save tokens immediately (even for new user)
                tokenStore.saveTokens(
                    access = data.accessToken,
                    refresh = data.refreshToken
                )
                Log.d("DEBUG", "Login success and tokens saved $data ")

                return data
            } else {
                throw IllegalStateException(
                    body.error ?: "Google login failed"
                )
            }
        } else {
            throw IllegalStateException(
                "Google login failed: ${response.errorMessage()}"
            )
        }
    }

    /**
     * Complete profile for new users.
     * Called only if isNewUser == true
     */
    suspend fun completeProfile(
        name: String?,
        phone: String?
    ): User {
        val response = api.completeProfile(
            CompleteProfileRequest(
                name = name,
                phone = phone
            )
        )
        // No token change required
        // authState will already be Authenticated

        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!

            if (body.success && body.data != null) {
                // Backend returns updated user
                return body.data
            } else {
                throw IllegalStateException(
                    body.error ?: "Failed to complete profile"
                )
            }
        } else {
            throw IllegalStateException(
                "Profile update failed: ${response.errorMessage()}"
            )
        }
    }

    /**
     * Explicit logout
     */
    suspend fun logout() {
        val refreshToken = tokenStore.getRefreshToken()
        Log.d("DEBUG", "Logging out with refresh token $refreshToken")
        try {
            refreshToken?.let { LogoutRequest(it) }?.let {
                api.logout(
                    body = it
                )
            }
        } catch (e: Exception) {
            // Even if this fails, we STILL clear local state
        } finally {
            tokenStore.clear()
        }
    }

    suspend fun delete() {
        val refreshToken = tokenStore.getRefreshToken()
        Log.d("DEBUG", "Deleting account with refresh token $refreshToken")
        try {
            refreshToken?.let { LogoutRequest(it) }?.let {
                api.delete(
                    body = it
                )
            }

        } catch (e: Exception) {
            // Even if this fails, we STILL clear local state
        } finally {
            tokenStore.clear()
        }
    }
}
