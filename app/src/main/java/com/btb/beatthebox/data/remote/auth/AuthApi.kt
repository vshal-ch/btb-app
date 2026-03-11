package com.btb.beatthebox.data.remote.auth

import com.btb.beatthebox.data.model.login.RefreshTokenRequest
import com.btb.beatthebox.data.model.login.RefreshTokenResponse
import com.btb.beatthebox.utils.ApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/refresh")
    fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Call<ApiResponse<RefreshTokenResponse>>
}