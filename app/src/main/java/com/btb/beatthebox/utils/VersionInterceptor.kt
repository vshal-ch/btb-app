package com.btb.beatthebox.utils

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class VersionInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionName = packageInfo.versionName
        val versionCode = packageInfo.longVersionCode

        val newRequest = chain.request().newBuilder()
            .addHeader("X-App-Version-Name", versionName ?: "null")
            .addHeader("X-App-Version-Code", versionCode.toString())
            .build()

        return chain.proceed(newRequest)
    }
}