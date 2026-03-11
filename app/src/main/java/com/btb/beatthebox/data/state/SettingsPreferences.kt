package com.btb.beatthebox.data.state

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.btb.beatthebox.core.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore("settings")

class SettingsPreferences(private val context: Context) {

    companion object {
        private val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val themeMode: Flow<ThemeMode> =
        context.dataStore.data.map { prefs ->
            ThemeMode.valueOf(
                prefs[THEME_MODE] ?: ThemeMode.SYSTEM.name
            )
        }

    fun getThemeModeBlocking(): ThemeMode =
        runBlocking {
            themeMode.first()
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE] = mode.name
        }
    }
}
