package com.btb.beatthebox

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.btb.beatthebox.core.theme.ThemeManager
import com.btb.beatthebox.data.remote.ApiClient
import com.btb.beatthebox.data.state.SettingsPreferences
import com.btb.beatthebox.di.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {

    override fun onCreate() {
        val prefs = SettingsPreferences(this)

        CoroutineScope(Dispatchers.Main).launch {
            prefs.themeMode.collect { mode ->
                ThemeManager.applyTheme(mode)
            }
        }
        super.onCreate()

        ServiceLocator.init(this)
    }
}