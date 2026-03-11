package com.btb.beatthebox.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.btb.beatthebox.core.theme.ThemeManager
import com.btb.beatthebox.core.theme.ThemeMode
import com.btb.beatthebox.data.state.SettingsPreferences
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val prefs: SettingsPreferences
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> =
        prefs.themeMode.stateIn(
            viewModelScope,
            kotlinx.coroutines.flow.SharingStarted.Eagerly,
            ThemeMode.SYSTEM
        )

    fun onThemeSelected(mode: ThemeMode) {
        viewModelScope.launch {
            prefs.setThemeMode(mode)
            ThemeManager.applyTheme(mode)
        }
    }

    class Factory(
        private val prefs: SettingsPreferences
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(prefs) as T
            }
            throw IllegalArgumentException("Unknown ViewModel")
        }
    }
}
