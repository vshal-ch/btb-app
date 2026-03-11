package com.btb.beatthebox.data.remote

import android.content.Context
import com.btb.beatthebox.constants.API_BASE_URL
import com.btb.beatthebox.data.remote.auth.AuthApiClient
import com.btb.beatthebox.di.ServiceLocator
import com.btb.beatthebox.utils.HeaderInterceptor
import com.btb.beatthebox.utils.TokenRefreshAuthenticator
import com.btb.beatthebox.utils.VersionInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = API_BASE_URL

    private lateinit var retrofit: Retrofit

    fun create(okHttpClient: OkHttpClient): ApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

//    fun init(context: Context) {
//        val logging = HttpLoggingInterceptor().apply {
//            level = HttpLoggingInterceptor.Level.BODY
//        }
//
//        val authApi = AuthApiClient.create()
//
//        val client = OkHttpClient.Builder()
//            .addInterceptor(logging)
//            .addInterceptor(VersionInterceptor(context.applicationContext)) // ✅ safe
//            .addInterceptor(HeaderInterceptor(ServiceLocator.tokenStore))
//            .authenticator(TokenRefreshAuthenticator(ServiceLocator.tokenStore, authApi))
//            .connectTimeout(30, TimeUnit.SECONDS)
//            .readTimeout(30, TimeUnit.SECONDS)
//            .build()
//
//        retrofit = Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(client)
//            .build()
//    }
}
