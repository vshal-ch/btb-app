package com.btb.beatthebox.data.model.login

import com.btb.beatthebox.data.model.User

data class GoogleLoginResponse(
    val accessToken: String,
    val refreshToken: String?,
    val user: User
)
