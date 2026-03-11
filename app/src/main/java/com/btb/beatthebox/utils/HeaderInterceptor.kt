package com.btb.beatthebox.utils

import com.btb.beatthebox.auth.TokenStore
import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor(private val tokenStore: TokenStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        var newRequestBuilder = originalRequest.newBuilder()
        newRequestBuilder = newRequestBuilder.header("x-platform", "421")

        val accessToken = tokenStore.getAccessTokenBlocking()

        if (!accessToken.isNullOrEmpty()) {
            newRequestBuilder = newRequestBuilder
                .header("Authorization", "Bearer $accessToken")
        }
        val newRequest = newRequestBuilder.build()


        return chain.proceed(newRequest)


    }
}