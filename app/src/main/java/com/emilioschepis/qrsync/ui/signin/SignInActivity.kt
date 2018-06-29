package com.emilioschepis.qrsync.ui.signin

import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import com.emilioschepis.qrsync.R
import com.emilioschepis.qrsync.model.QSError
import com.emilioschepis.qrsync.ui.codelist.CodeListActivity
import com.emilioschepis.qrsync.ui.signup.SignUpActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import org.koin.android.architecture.ext.viewModel

class SignInActivity : AppCompatActivity() {

    private val viewModel by viewModel<SignInViewModel>()
    private var googleApiClient: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        findViewById<Button>(R.id.sign_in_submit_btn).setOnClickListener {
            // When the Sign In button is clicked, the TIL text is
            // sent to the ViewModel for validation
            val email = findViewById<TextInputEditText>(R.id.sign_in_email_iet).text.toString()
            val password = findViewById<TextInputEditText>(R.id.sign_in_password_iet).text.toString()

            viewModel.authenticate(email, password).observe(this, Observer {
                it?.fold(this::onAuthenticationError) { onAuthenticationSuccess() }
            })
        }

        findViewById<Button>(R.id.sign_in_create_account_btn).setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        findViewById<Button>(R.id.sign_in_google_btn).setOnClickListener {
            configureGoogleSignIn()
            startActivityForResult(googleSignInIntent, 6006)
        }

        viewModel.loading.observe(this, Observer {
            it?.let {
                val progressBar = findViewById<ProgressBar>(R.id.sign_in_loading_prb)
                val submitBtn = findViewById<Button>(R.id.sign_in_submit_btn)
                val googleBtn = findViewById<Button>(R.id.sign_in_google_btn)
                if (it) {
                    progressBar.visibility = View.VISIBLE
                    submitBtn.visibility = View.GONE
                    googleBtn.visibility = View.GONE
                } else {
                    progressBar.visibility = View.GONE
                    submitBtn.visibility = View.VISIBLE
                    googleBtn.visibility = View.VISIBLE
                }
            }
        })
    }

    override fun onPause() {
        googleApiClient?.stopAutoManage(this)
        googleApiClient?.disconnect()
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            6006 -> {
                if (resultCode == Activity.RESULT_OK) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    try {
                        viewModel.authenticateWithGoogle(task.result).observe(this, Observer {
                            it?.fold(this::onAuthenticationError) { onAuthenticationSuccess() }
                        })
                    } catch (ex: ApiException) {
                        snackbarMessage(getString(R.string.error_generic_unknown_reason, ex.message)).show()
                    }
                }
            }
        }
    }

    private fun clearErrors() {
        findViewById<TextInputLayout>(R.id.sign_in_email_til).error = null
        findViewById<TextInputLayout>(R.id.sign_in_password_til).error = null
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleApiClient = GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .enableAutoManage(this) {}
                .build()
    }

    private fun onAuthenticationError(error: QSError) {
        clearErrors()
        when (error) {
            is QSError.AuthenticationError -> {
                error.associatedView?.error = getString(error.resId)
            }
            else -> {
                snackbarMessage(getString(error.resId, error.params.getOrNull(0))).show()
            }
        }
    }

    private fun onAuthenticationSuccess() {
        startActivity(Intent(this, CodeListActivity::class.java))
        finish()
    }

    private fun snackbarMessage(message: String,
                                duration: Int = Snackbar.LENGTH_INDEFINITE,
                                callback: (() -> Unit)? = null): Snackbar {
        val root = findViewById<CoordinatorLayout>(R.id.sign_in_root_cdl)
        val snackbar = Snackbar.make(root, message, duration)

        if (duration == Snackbar.LENGTH_INDEFINITE) {
            snackbar.setAction(android.R.string.ok) { callback?.invoke() }
        }

        return snackbar
    }

    private val googleSignInIntent: Intent
        get() {
            return Auth.GoogleSignInApi
                    .getSignInIntent(googleApiClient)
        }

    private val QSError.AuthenticationError.associatedView: TextInputLayout?
        get() {
            return when (this) {
                is QSError.AuthenticationError.InvalidUser ->
                    findViewById(R.id.sign_in_email_til)
                is QSError.AuthenticationError.InvalidCredentials ->
                    findViewById(R.id.sign_in_password_til)
                is QSError.AuthenticationError.EmptyEmail ->
                    findViewById(R.id.sign_in_email_til)
                is QSError.AuthenticationError.EmptyPassword ->
                    findViewById(R.id.sign_in_password_til)
                is QSError.AuthenticationError.MalformedEmail ->
                    findViewById(R.id.sign_in_email_til)
                is QSError.AuthenticationError.WeakPassword ->
                    findViewById(R.id.sign_in_password_til)
                else -> null
            }
        }
}