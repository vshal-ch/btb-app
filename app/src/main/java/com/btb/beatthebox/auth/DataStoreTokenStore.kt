package com.btb.beatthebox.auth

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class DataStoreTokenStore(
    private val dataStore: DataStore<Preferences>
) : TokenStore {

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }

    override val accessTokenFlow: Flow<String?> = dataStore.data.map { prefs ->
        prefs[ACCESS_TOKEN]
    }

    override suspend fun getAccessToken(): String? {
        Log.d("Data store", dataStore.data.first().toString())
        return dataStore.data.first()[ACCESS_TOKEN]
    }

    override suspend fun getRefreshToken(): String? {
        return dataStore.data.first()[REFRESH_TOKEN]
    }

    override suspend fun saveTokens(
        access: String, refresh: String?
    ) {
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = access
            refresh?.let {
                prefs[REFRESH_TOKEN] = it
            }
        }
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    override fun getAccessTokenBlocking(): String? =
        runBlocking {
            dataStore.data.first()[ACCESS_TOKEN]
        }

    override fun getRefreshTokenBlocking(): String? =
        runBlocking {
            dataStore.data.first()[REFRESH_TOKEN]
        }

    override fun saveTokensBlocking(access: String, refresh: String?) {
        runBlocking {
            dataStore.edit {
                it[ACCESS_TOKEN] = access
                refresh?.let { r -> it[REFRESH_TOKEN] = r }
            }
        }
    }

    override fun clearBlocking() {
        runBlocking {
            dataStore.edit { it.clear() }
        }
    }
}