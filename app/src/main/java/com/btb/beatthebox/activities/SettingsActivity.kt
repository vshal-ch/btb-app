package com.btb.beatthebox.activities

import android.os.Bundle
import android.widget.ImageView
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.btb.beatthebox.R
import com.btb.beatthebox.core.theme.ThemeMode
import com.btb.beatthebox.data.state.SettingsPreferences
import com.btb.beatthebox.data.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity(R.layout.activity_settings) {

    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            SettingsViewModel.Factory(
                SettingsPreferences(this)
            )
        )[SettingsViewModel::class.java]

        val light = findViewById<RadioButton>(R.id.themeLight)
        val dark = findViewById<RadioButton>(R.id.themeDark)
        val system = findViewById<RadioButton>(R.id.themeSystem)

        light.setOnClickListener { viewModel.onThemeSelected(ThemeMode.LIGHT) }
        dark.setOnClickListener { viewModel.onThemeSelected(ThemeMode.DARK) }
        system.setOnClickListener { viewModel.onThemeSelected(ThemeMode.SYSTEM) }
        findViewById<ImageView>(R.id.backBtn).setOnClickListener {
            finish()
        }


        lifecycleScope.launch {
            viewModel.themeMode.collect { mode ->
                light.isChecked = mode == ThemeMode.LIGHT
                dark.isChecked = mode == ThemeMode.DARK
                system.isChecked = mode == ThemeMode.SYSTEM
            }
        }
    }
}
