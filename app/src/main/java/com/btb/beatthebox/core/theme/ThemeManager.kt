package com.btb.beatthebox.core.theme

import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {

    fun applyTheme(mode: ThemeMode) {
        when (mode) {
            ThemeMode.LIGHT ->
                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO
                )

            ThemeMode.DARK ->
                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES
                )

            ThemeMode.SYSTEM ->
                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                )
        }
    }
}
