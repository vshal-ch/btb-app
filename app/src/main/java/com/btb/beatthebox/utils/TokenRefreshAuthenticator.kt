package com.btb.beatthebox.utils

import android.util.Log
import com.btb.beatthebox.auth.TokenStore
import com.btb.beatthebox.data.remote.auth.AuthApi
import com.btb.beatthebox.data.remote.auth.refreshTokenBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenRefreshAuthenticator(
    private val tokenStore: TokenStore,
    private val api: AuthApi // refresh endpoint ONLY
) : Authenticator {

    @Synchronized
    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d("TokenRefreshAuthenticator", "Inside authenticate")
        // 1️⃣ Prevent infinite retry loops
        if (responseCount(response) >= 2) {
            tokenStore.clearBlocking()
            return null
        }

        val refreshToken = tokenStore.getRefreshTokenBlocking()
            ?: return null

        // 2️⃣ Refresh token (blocking call)
        val newTokens = try {
            api.refreshTokenBlocking(refreshToken)
        } catch (e: Exception) {
            Log.e("TokenRefreshAuthenticator", "Token refresh failed", e)
//            tokenStore.clearBlocking()
            return null
        }

        // 3️⃣ Save new tokens
        tokenStore.saveTokensBlocking(
            access = newTokens.accessToken,
            refresh = newTokens.refreshToken
        )

        // 4️⃣ Retry original request with new token
        return response.request
            .newBuilder()
            .header("Authorization", "Bearer ${newTokens.accessToken}")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
