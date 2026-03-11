package com.btb.beatthebox.activities // Your package name

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.btb.beatthebox.App
import com.btb.beatthebox.R
import com.btb.beatthebox.data.model.StartupState
import com.btb.beatthebox.data.remote.ApiClient
import com.btb.beatthebox.data.viewmodel.LoginViewModel
import com.btb.beatthebox.data.viewmodel.StartupViewModel
import com.btb.beatthebox.data.viewmodel.StartupViewModelFactory
import com.btb.beatthebox.data.viewmodel.UserViewModel
import com.btb.beatthebox.di.ServiceLocator
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallException
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.launch
import com.btb.beatthebox.data.state.UserSession

class MainActivity : AppCompatActivity() {

    private val appUpdateManager: AppUpdateManager by lazy {
        AppUpdateManagerFactory.create(this)
    }

    private val startupViewModel: StartupViewModel by viewModels {
        StartupViewModelFactory(
            authRepository = ServiceLocator.authRepository,
            appUpdateManager = appUpdateManager,
            userSession = UserSession
        )
    }

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            popupSnackbarForCompleteUpdate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        splashScreen.setKeepOnScreenCondition {
            startupViewModel.state.value is StartupState.Loading
        }

        observeStartup()
    }

    private fun observeStartup() {
        lifecycleScope.launch {
            startupViewModel.state.collect { state ->
                when (state) {

                    StartupState.UpdateRequired -> {
                        startImmediateUpdate()
                    }

                    StartupState.GoToHome -> {
                        openMainUI()
                    }

                    StartupState.GoToLogin -> {
                        openLoginUI()
                    }

                    StartupState.GoToPhoneInput -> {
                        openLoginUI()
                    }

                    is StartupState.Error -> {
                        Toast.makeText(this@MainActivity, state.message, Toast.LENGTH_LONG).show()
                        openLoginUI()
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun startImmediateUpdate() {
        startupViewModel.registerUpdateListener(installStateUpdatedListener)

        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            appUpdateManager.startUpdateFlowForResult(
                info,
                AppUpdateType.IMMEDIATE,
                this,
                1001
            )
        }
    }

    override fun onResume() {
        super.onResume()

        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() ==
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            ) {
                startImmediateUpdate()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        startupViewModel.unregisterUpdateListener(installStateUpdatedListener)
    }

    private fun popupSnackbarForCompleteUpdate() {
        Snackbar.make(
            findViewById(R.id.main_root),
            "Update downloaded",
            Snackbar.LENGTH_INDEFINITE
        ).setAction("RESTART") {
            appUpdateManager.completeUpdate()
        }.show()
    }

    private fun openMainUI() {
        val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    private fun openLoginUI() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}
