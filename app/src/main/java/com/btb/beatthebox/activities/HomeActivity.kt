package com.btb.beatthebox.activities // Your package name

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.btb.beatthebox.R
import com.btb.beatthebox.data.remote.ApiClient
import com.btb.beatthebox.ui.shared.MainViewModel
import com.btb.beatthebox.utils.ApiResult
import com.cashfree.pg.api.CFPaymentGatewayService
import com.cashfree.pg.core.api.CFSession
import com.cashfree.pg.core.api.CFTheme
import com.cashfree.pg.core.api.callback.CFCheckoutResponseCallback
import com.cashfree.pg.core.api.utils.CFErrorResponse
import com.cashfree.pg.core.api.webcheckout.CFWebCheckoutPayment
import com.cashfree.pg.core.api.webcheckout.CFWebCheckoutTheme.CFWebCheckoutThemeBuilder
import com.cashfree.pg.ui.api.upi.intent.CFIntentTheme
import com.cashfree.pg.ui.api.upi.intent.CFUPIIntentCheckout
import com.cashfree.pg.ui.api.upi.intent.CFUPIIntentCheckout.CFUPIIntentBuilder
import com.cashfree.pg.ui.api.upi.intent.CFUPIIntentCheckoutPayment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallException
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import java.util.Arrays

class HomeActivity : AppCompatActivity(), CFCheckoutResponseCallback {

    private lateinit var navController: NavController

    private lateinit var progressBar: ProgressBar
    private lateinit var bottomNavigationView: BottomNavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        progressBar = findViewById(R.id.initial_loading_progressbar)
        bottomNavigationView = findViewById(R.id.bottom_navigation_view)

        CFPaymentGatewayService.getInstance().setCheckoutCallback(this)

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        bottomNavigationView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_slot_availability,
                R.id.bannerDetailFragment,
                R.id.slot_selection_fragment,
                R.id.navigation_checkout,
                R.id.verifyingPaymentFragment,
                R.id.paymentResultFragment,
                R.id.bookingPassFragment,
                R.id.navigation_policy_webview,
                R.id.navigation_faq,
                R.id.navigation_sport_cost_detail,
                R.id.userGamesFragment,
                R.id.rescheduleFragment,
                     R.id.privacyPolicy-> {
                    bottomNavigationView.visibility = View.GONE

                }

                else -> {
                    bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onPaymentVerify(orderID: String?) {
        Toast.makeText(this, "Payment success", Toast.LENGTH_SHORT).show()
        if (orderID == null) return
        navController
            .navigate(
                R.id.verifyingPaymentFragment,
                bundleOf("orderId" to orderID)
            )
    }

    override fun onPaymentFailure(cfErrorResponse: CFErrorResponse?, orderID: String?) {
        Toast.makeText(this, "Payment failure", Toast.LENGTH_SHORT).show()

        navController
            .navigate(
                R.id.paymentResultFragment
            )
    }

    fun startCashfreePayment(orderId: String, paymentSessionId: String) {
        val cfSession = CFSession.CFSessionBuilder()
            .setEnvironment(CFSession.Environment.PRODUCTION) // PROD later
            .setOrderId(orderId)
            .setPaymentSessionID(paymentSessionId)
            .build()

        val cfTheme = CFWebCheckoutThemeBuilder()
            .setNavigationBarTextColor("#000000")
            .setNavigationBarBackgroundColor("#FFFFFF")
            .build();

        val payment = CFWebCheckoutPayment.CFWebCheckoutPaymentBuilder()
            .setSession(cfSession)
            .setCFWebCheckoutUITheme(cfTheme)
            .build()

        CFPaymentGatewayService.getInstance()
            .doPayment(this, payment)
    }

//    private fun checkForAppUpdate() {
//        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
//            when {
//                appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
//                    try {
//                        startAppUpdateFlow(
//                            appUpdateInfo, AppUpdateType.IMMEDIATE, MY_REQUEST_CODE_IMMEDIATE
//                        )
//                    } catch (e: Exception) {
//                        Log.e("MainActivity", "Error resuming immediate update", e)
//                    }
//                }
//
//                appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED -> {
//                    popupSnackbarForCompleteUpdate()
//                }
//
//                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(
//                    AppUpdateType.FLEXIBLE
//                ) && appUpdateInfo.installStatus() == InstallStatus.DOWNLOADING -> {
//                    try {
//                        appUpdateManager.registerListener(installStateUpdatedListener)
//                    } catch (e: Exception) {
//                        Log.w("MainActivity", "Listener registration error: ${e.message}")
//                    }
//                }
//
//                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(
//                    AppUpdateType.IMMEDIATE
//                ) -> {
//                    startAppUpdateFlow(
//                        appUpdateInfo, AppUpdateType.IMMEDIATE, MY_REQUEST_CODE_IMMEDIATE
//                    )
//                }
//
//                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(
//                    AppUpdateType.FLEXIBLE
//                ) -> {
//                    appUpdateManager.registerListener(installStateUpdatedListener)
//                    startAppUpdateFlow(
//                        appUpdateInfo, AppUpdateType.FLEXIBLE, MY_REQUEST_CODE_FLEXIBLE
//                    )
//                }
//
//                appUpdateInfo.updateAvailability() == UpdateAvailability.UNKNOWN || appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
//                    Log.d("MainActivity", "No update available")
//                    progressBar.isVisible = false
//                }
//            }
//        }.addOnFailureListener { e ->
//            if (e is InstallException) {
//                Log.e("MainActivity", "Update check failed: ${e.errorCode}")
//            } else {
//                Log.e("MainActivity", "Update check failed: ${e.message}")
//            }
//        }
//    }

//    private fun startAppUpdateFlow(
//        appUpdateInfo: AppUpdateInfo, appUpdateType: Int, requestCode: Int
//    ) {
//        try {
//            appUpdateManager.startUpdateFlowForResult(
//                appUpdateInfo, appUpdateType, this, requestCode
//            )
//        } catch (e: Exception) {
//            Log.e("MainActivity", "startAppUpdateFlow failed for type $appUpdateType: ${e.message}")
//        }
//    }
//
//    @Deprecated("Deprecated in AndroidX but still works")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == MY_REQUEST_CODE_FLEXIBLE) {
//            if (resultCode != Activity.RESULT_OK) {
//                Log.w("MainActivity", "Flexible update flow failed! Result code: $resultCode")
//            }
//        } else if (requestCode == MY_REQUEST_CODE_IMMEDIATE) {
//            if (resultCode != Activity.RESULT_OK) {
//                Log.w("MainActivity", "Immediate update flow failed! Result code: $resultCode")
//                Toast.makeText(this, "Update required. Closing app.", Toast.LENGTH_LONG).show()
//                finish() // Close app to enforce force update
//            }
//        }
//    }
//
//    private fun popupSnackbarForCompleteUpdate() {
//        val rootView = findViewById<android.view.View>(R.id.main_coordinator_layout)
//            ?: window.decorView.findViewById(android.R.id.content)
//        Snackbar.make(
//            rootView, "An update has just been downloaded.", Snackbar.LENGTH_INDEFINITE
//        ).apply {
//            setAction("RESTART") { appUpdateManager.completeUpdate() }
//            setActionTextColor(
//                resources.getColor(
//                    R.color.dark_green_primary, theme
//                )
//            )
//            show()
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        checkForAppUpdate()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        try {
//            appUpdateManager.unregisterListener(installStateUpdatedListener)
//        } catch (e: Exception) {
//            Log.w("MainActivity", "Error unregistering install state listener: ${e.message}")
//        }
//    }
}