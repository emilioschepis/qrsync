package com.emilioschepis.qrsync.ui.signup

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
import org.koin.android.architecture.ext.viewModel

class SignUpActivity : AppCompatActivity() {

    private val viewModel by viewModel<SignUpViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        findViewById<Button>(R.id.sign_up_submit_btn).setOnClickListener {
            // When the Sign Up button is clicked, the TIL text is
            // sent to the ViewModel for validation
            val email = findViewById<TextInputEditText>(R.id.sign_up_email_iet).text.toString()
            val password = findViewById<TextInputEditText>(R.id.sign_up_password_iet).text.toString()

            viewModel.register(email, password).observe(this, Observer {
                it?.fold(this::onRegistrationError) { onRegistrationSuccess() }
            })
        }

        viewModel.loading.observe(this, Observer {
            it?.let {
                val progressBar = findViewById<ProgressBar>(R.id.sign_up_loading_prb)
                val submitBtn = findViewById<Button>(R.id.sign_up_submit_btn)
                if (it) {
                    progressBar.visibility = View.VISIBLE
                    submitBtn.visibility = View.GONE
                } else {
                    progressBar.visibility = View.GONE
                    submitBtn.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun clearErrors() {
        findViewById<TextInputLayout>(R.id.sign_up_email_til).error = null
        findViewById<TextInputLayout>(R.id.sign_up_password_til).error = null
    }

    private fun onRegistrationError(error: QSError) {
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

    private fun onRegistrationSuccess() {
        startActivity(Intent(this, CodeListActivity::class.java))
        finishAffinity()
    }

    private fun snackbarMessage(message: String,
                                duration: Int = Snackbar.LENGTH_INDEFINITE,
                                callback: (() -> Unit)? = null): Snackbar {
        val root = findViewById<CoordinatorLayout>(R.id.sign_up_root_cdl)
        val snackbar = Snackbar.make(root, message, duration)

        if (duration == Snackbar.LENGTH_INDEFINITE) {
            snackbar.setAction(android.R.string.ok) { callback?.invoke() }
        }

        return snackbar
    }

    private val QSError.AuthenticationError.associatedView: TextInputLayout?
        get() {
            return when (this) {
                is QSError.AuthenticationError.EmptyEmail ->
                    findViewById(R.id.sign_up_email_til)
                is QSError.AuthenticationError.MalformedEmail ->
                    findViewById(R.id.sign_up_email_til)
                is QSError.AuthenticationError.UserCollision ->
                    findViewById(R.id.sign_up_email_til)
                is QSError.AuthenticationError.EmptyPassword ->
                    findViewById(R.id.sign_up_password_til)
                is QSError.AuthenticationError.WeakPassword ->
                    findViewById(R.id.sign_up_password_til)
                else -> null
            }
        }
}
