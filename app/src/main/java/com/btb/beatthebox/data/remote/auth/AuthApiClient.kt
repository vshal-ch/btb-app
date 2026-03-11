package com.btb.beatthebox.data.remote.auth

import com.btb.beatthebox.auth.TokenStore
import com.btb.beatthebox.constants.API_BASE_URL
import com.btb.beatthebox.utils.HeaderInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AuthApiClient {
    private const val BASE_URL = API_BASE_URL

    fun create(tokenStore: TokenStore): AuthApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder().addInterceptor(logging).addInterceptor(HeaderInterceptor(tokenStore)).build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }
}