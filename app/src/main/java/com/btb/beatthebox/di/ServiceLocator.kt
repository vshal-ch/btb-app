package com.btb.beatthebox.di

import android.content.Context
import com.btb.beatthebox.auth.AuthRepository
import com.btb.beatthebox.data.remote.ApiClient
import androidx.datastore.preferences.preferencesDataStore
import com.btb.beatthebox.auth.DataStoreTokenStore
import com.btb.beatthebox.auth.TokenStore
import com.btb.beatthebox.data.remote.ApiService
import com.btb.beatthebox.data.remote.auth.AuthApi
import com.btb.beatthebox.data.remote.auth.AuthApiClient
import com.btb.beatthebox.data.repository.BookingRepository
import com.btb.beatthebox.data.repository.CostRepository
import com.btb.beatthebox.data.repository.PaymentRepository
import com.btb.beatthebox.utils.HeaderInterceptor
import com.btb.beatthebox.utils.TokenRefreshAuthenticator
import com.btb.beatthebox.utils.VersionInterceptor
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File

private val Context.dataStore by preferencesDataStore("auth_prefs")

object ServiceLocator {

    private var initialized = false

    lateinit var tokenStore: TokenStore
        private set

    lateinit var apiService: ApiService
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var bookingRepository: BookingRepository
        private set

    lateinit var costRepository: CostRepository
        private set

    lateinit var paymentRepository: PaymentRepository
        private set

    fun init(context: Context) {
        if (initialized) return

        val appContext = context.applicationContext

        tokenStore = DataStoreTokenStore(appContext.dataStore)

        val authApi: AuthApi = AuthApiClient.create(tokenStore)

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val singleEndpointCacheInterceptor = Interceptor { chain ->
            val request = chain.request()
            val hasCacheHeader = request.header("X-Local-Cache") != null

            val response = chain.proceed(request)

            if (hasCacheHeader) {
                // Force cache this specific response for 1 hour (3600 seconds)
                response.newBuilder()
                    .header("Cache-Control", "public, max-age=3600")
                    .removeHeader("Pragma") // Clear legacy headers that block caching
                    .build()
            } else {
                response
            }
        }

        val cacheSize = (10 * 1024 * 1024).toLong() // 10 MB allocation
        val myCache = Cache(File(context.cacheDir, "http_cache"), cacheSize)

        val okHttpClient = OkHttpClient.Builder()
            .cache(myCache)
            .addInterceptor(logging)
            .addInterceptor(VersionInterceptor(appContext))
            .addInterceptor(HeaderInterceptor(tokenStore))
            .addInterceptor(singleEndpointCacheInterceptor)
            .authenticator(
                TokenRefreshAuthenticator(
                    tokenStore = tokenStore,
                    api = authApi
                )
            )
            .build()

        apiService = ApiClient.create(okHttpClient)

        authRepository = AuthRepository(
            tokenStore = tokenStore, api = apiService
        )

        bookingRepository = BookingRepository(apiService)
        costRepository = CostRepository(apiService)
        paymentRepository = PaymentRepository(apiService)

        initialized = true
    }
}
