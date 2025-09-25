package com.btb.beatthebox.activities // Your package name

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.btb.beatthebox.R
import com.btb.beatthebox.data.remote.ApiClient
import com.btb.beatthebox.ui.shared.MainViewModel
import com.btb.beatthebox.utils.ApiResult
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallException
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private val mainViewModel: MainViewModel by viewModels() // Using activity-ktx for ViewModel

    private lateinit var progressBar: ProgressBar
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var appUpdateManager: AppUpdateManager
    private val MY_REQUEST_CODE_FLEXIBLE = 123 // Request code for flexible in-app update
    private val MY_REQUEST_CODE_IMMEDIATE = 124 // Request code for immediate in-app update

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            popupSnackbarForCompleteUpdate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(this)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.initial_loading_progressbar)
        bottomNavigationView = findViewById(R.id.bottom_navigation_view)

        setupNavigation()
        observeVenueDetails()

        progressBar.isVisible = true
        mainViewModel.fetchVenueDetails()

        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        checkForAppUpdate()
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        bottomNavigationView.setupWithNavController(navController)
    }

    private fun observeVenueDetails() {
        mainViewModel.venueDetails.observe(this) { result ->
            when (result) {
                is ApiResult.Loading -> {
                    Log.d("MainActivity", "Venue details loading...")
                }

                is ApiResult.Success -> {
                    progressBar.isVisible = false // Hide loading indicator
                }

                is ApiResult.Error -> {
                    progressBar.isVisible = false // Hide loading indicator
                    Toast.makeText(
                        this, "Failed to load venue details: ${result.message}", Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun checkForAppUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            when {
                appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                    try {
                        startAppUpdateFlow(
                            appUpdateInfo, AppUpdateType.IMMEDIATE, MY_REQUEST_CODE_IMMEDIATE
                        )
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error resuming immediate update", e)
                    }
                }

                appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED -> {
                    popupSnackbarForCompleteUpdate()
                }

                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(
                    AppUpdateType.FLEXIBLE
                ) && appUpdateInfo.installStatus() == InstallStatus.DOWNLOADING -> {
                    try {
                        appUpdateManager.registerListener(installStateUpdatedListener)
                    } catch (e: Exception) {
                        Log.w("MainActivity", "Listener registration error: ${e.message}")
                    }
                }

                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(
                    AppUpdateType.IMMEDIATE
                ) -> {
                    startAppUpdateFlow(
                        appUpdateInfo, AppUpdateType.IMMEDIATE, MY_REQUEST_CODE_IMMEDIATE
                    )
                }

                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(
                    AppUpdateType.FLEXIBLE
                ) -> {
                    appUpdateManager.registerListener(installStateUpdatedListener)
                    startAppUpdateFlow(
                        appUpdateInfo, AppUpdateType.FLEXIBLE, MY_REQUEST_CODE_FLEXIBLE
                    )
                }

                appUpdateInfo.updateAvailability() == UpdateAvailability.UNKNOWN || appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                    Log.d("MainActivity", "No update available")
                    progressBar.isVisible = false
                }
            }
        }.addOnFailureListener { e ->
            if (e is InstallException) {
                Log.e("MainActivity", "Update check failed: ${e.errorCode}")
            } else {
                Log.e("MainActivity", "Update check failed: ${e.message}")
            }
        }
    }

    private fun startAppUpdateFlow(
        appUpdateInfo: AppUpdateInfo, appUpdateType: Int, requestCode: Int
    ) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo, appUpdateType, this, requestCode
            )
        } catch (e: Exception) {
            Log.e("MainActivity", "startAppUpdateFlow failed for type $appUpdateType: ${e.message}")
        }
    }

    @Deprecated("Deprecated in AndroidX but still works")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_REQUEST_CODE_FLEXIBLE) {
            if (resultCode != Activity.RESULT_OK) {
                Log.w("MainActivity", "Flexible update flow failed! Result code: $resultCode")
            }
        } else if (requestCode == MY_REQUEST_CODE_IMMEDIATE) {
            if (resultCode != Activity.RESULT_OK) {
                Log.w("MainActivity", "Immediate update flow failed! Result code: $resultCode")
                Toast.makeText(this, "Update required. Closing app.", Toast.LENGTH_LONG).show()
                finish() // Close app to enforce force update
            }
        }
    }

    private fun popupSnackbarForCompleteUpdate() {
        val rootView = findViewById<android.view.View>(R.id.main_coordinator_layout)
            ?: window.decorView.findViewById(android.R.id.content)
        Snackbar.make(
            rootView, "An update has just been downloaded.", Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("RESTART") { appUpdateManager.completeUpdate() }
            setActionTextColor(
                resources.getColor(
                    R.color.dark_green_primary, theme
                )
            )
            show()
        }
    }

    override fun onResume() {
        super.onResume()
        checkForAppUpdate()
    }

    override fun onPause() {
        super.onPause()
        try {
            appUpdateManager.unregisterListener(installStateUpdatedListener)
        } catch (e: Exception) {
            Log.w("MainActivity", "Error unregistering install state listener: ${e.message}")
        }
    }
}