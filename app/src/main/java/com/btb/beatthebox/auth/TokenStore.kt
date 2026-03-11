package com.btb.beatthebox.auth

import kotlinx.coroutines.flow.Flow

interface TokenStore {
    val accessTokenFlow: Flow<String?>
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun saveTokens(access: String, refresh: String?)
    suspend fun clear()
    fun getAccessTokenBlocking(): String?
    fun getRefreshTokenBlocking(): String?
    fun saveTokensBlocking(access: String, refresh: String?)
    fun clearBlocking()
}