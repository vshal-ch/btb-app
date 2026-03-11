package com.btb.beatthebox.data.model.login

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String?
)