package com.btb.beatthebox.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.repeatOnLifecycle
import com.btb.beatthebox.R
import com.btb.beatthebox.data.state.LoginState
import com.btb.beatthebox.data.state.UserSession
import com.btb.beatthebox.data.viewmodel.LoginViewModel
import com.btb.beatthebox.data.viewmodel.UserViewModel
import com.btb.beatthebox.di.ServiceLocator
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import com.google.android.gms.common.SignInButton

class LoginActivity : AppCompatActivity() {

    private val TAG = "LOGIN_ACTIVITY"

    private val userViewModel: UserViewModel by lazy {
        ViewModelProvider(this)[UserViewModel::class.java]
    }

    private val loginViewModel: LoginViewModel by viewModels {
        LoginViewModel.Factory(
            ServiceLocator.authRepository,
            UserSession
        )
    }

    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        credentialManager = CredentialManager.create(this)

        observeUser()
        observeLoginState()

        setupGoogleLoginClick()
        setupProfileSubmit()
    }

    private fun observeUser() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.user.collect { user ->
                    when {
                        user == null -> showGoogleLogin()
                        user.newUser -> showProfileForm()
                        else -> openMainUI()
                    }
                }
            }
        }
    }

    private fun observeLoginState() {
        lifecycleScope.launch {
            loginViewModel.state.collect { state ->
                when (state) {
                    is LoginState.Loading -> showLoading()
                    is LoginState.Error -> {
                        findViewById<View>(R.id.progress_bar).visibility = View.GONE
                        Toast.makeText(
                            this@LoginActivity,
                            state.message ?: "Something went wrong",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun setupGoogleLoginClick() {
        findViewById<SignInButton>(R.id.btnGoogleSignIn).setOnClickListener {
            startGoogleLogin()
        }
    }

    private fun startGoogleLogin() {
        val googleIdOption =
            GetSignInWithGoogleOption.Builder(
                getString(R.string.server_client_id)
            ).build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result =
                    credentialManager.getCredential(this@LoginActivity, request)
                val credential = result.credential

                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleCred =
                        GoogleIdTokenCredential.createFrom(credential.data)

                    loginViewModel.onGoogleLoginSuccess(
                        idToken = googleCred.idToken,
                    )
                }
            } catch (e: NoCredentialException) {
                Toast.makeText(this@LoginActivity, "No Google account found", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Credential error", e)
            }
        }
    }

    private fun setupProfileSubmit() {
        findViewById<View>(R.id.btn_continue).setOnClickListener {
            val name =
                findViewById<com.google.android.material.textfield.TextInputEditText>(
                    R.id.input_name
                ).text.toString()

            val phone =
                findViewById<com.google.android.material.textfield.TextInputEditText>(
                    R.id.input_phone
                ).text.toString()

            loginViewModel.submitProfile(name, phone)
        }
    }

    private fun showGoogleLogin() {
        findViewById<View>(R.id.container_google_login).visibility = View.VISIBLE
        findViewById<View>(R.id.container_profile_form).visibility = View.GONE
        findViewById<View>(R.id.progress_bar).visibility = View.GONE
    }

    private fun showProfileForm() {
        findViewById<View>(R.id.container_google_login).visibility = View.GONE
        findViewById<View>(R.id.container_profile_form).visibility = View.VISIBLE
        findViewById<View>(R.id.progress_bar).visibility = View.GONE

        userViewModel.user.value?.let { user ->
            findViewById<com.google.android.material.textfield.TextInputEditText>(
                R.id.input_name
            ).setText(user.name)

            findViewById<com.google.android.material.textfield.TextInputEditText>(
                R.id.input_email
            ).setText(user.email)
        }
    }

    private fun showLoading() {
        findViewById<View>(R.id.progress_bar).visibility = View.VISIBLE
    }

    private fun openMainUI() {
        startActivity(
            Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
    }
}
