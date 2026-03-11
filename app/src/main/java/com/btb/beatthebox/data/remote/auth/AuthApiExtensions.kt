package com.btb.beatthebox.data.remote.auth

import com.btb.beatthebox.data.model.login.RefreshTokenRequest
import com.btb.beatthebox.data.model.login.RefreshTokenResponse
import java.io.IOException


fun AuthApi.refreshTokenBlocking(
    refreshToken: String
): RefreshTokenResponse {

    val response = refreshToken(
        RefreshTokenRequest(refreshToken)
    ).execute()

    // HTTP-level failure
    if (!response.isSuccessful || response.body() == null) {
        throw IOException("Token refresh failed: HTTP ${response.code()}")
    }

    val envelope = response.body()!!  // ApiResponse<RefreshTokenResponse>

    // API-level failure
    if (!envelope.success || envelope.data == null) {
        throw IOException(envelope.error ?: "Token refresh failed")
    }

    val tokenData = envelope.data

    if (tokenData.accessToken.isBlank()) {
        throw IOException("Token refresh failed: access token missing")
    }

    return tokenData
}
