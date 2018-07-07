package com.emilioschepis.qrsync.ui.signup

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import com.emilioschepis.qrsync.R
import com.emilioschepis.qrsync.extension.snackbarError
import com.emilioschepis.qrsync.model.QSError
import com.emilioschepis.qrsync.ui.codelist.CodeListActivity
import org.koin.android.viewmodel.ext.android.viewModel

class SignUpActivity : AppCompatActivity() {

    private val viewModel by viewModel<SignUpViewModel>()

    private val root by lazy { findViewById<CoordinatorLayout>(R.id.sign_up_root_cdl) }
    private val progressBar by lazy { findViewById<ProgressBar>(R.id.sign_up_loading_prb) }
    private val emailTil by lazy { findViewById<TextInputLayout>(R.id.sign_up_email_til) }
    private val passwordTil by lazy { findViewById<TextInputLayout>(R.id.sign_up_password_til) }
    private val submitBtn by lazy { findViewById<Button>(R.id.sign_up_submit_btn) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        submitBtn.setOnClickListener {
            // When the Sign Up button is clicked, the TIL text is
            // sent to the ViewModel for validation
            val email = emailTil.editText?.text.toString()
            val password = passwordTil.editText?.text.toString()

            viewModel.register(email, password).observe(this, Observer {
                it?.fold(this::onRegistrationError) { onRegistrationSuccess() }
            })
        }

        viewModel.loading.observe(this, Observer {
            it?.let {
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
        emailTil.error = null
        passwordTil.error = null
    }

    private fun onRegistrationError(error: QSError) {
        clearErrors()
        when (error) {
            is QSError.AuthenticationError -> {
                error.associatedView?.error = getString(error.resId)
            }
            else -> {
                snackbarError(root, error)
            }
        }
    }

    private fun onRegistrationSuccess() {
        startActivity(Intent(this, CodeListActivity::class.java))
        finishAffinity()
    }

    private val QSError.AuthenticationError.associatedView: TextInputLayout?
        get() {
            return when (this) {
                QSError.AuthenticationError.EmptyEmail,
                QSError.AuthenticationError.MalformedEmail,
                QSError.AuthenticationError.UserCollision -> emailTil
                QSError.AuthenticationError.EmptyPassword,
                QSError.AuthenticationError.WeakPassword -> passwordTil
                else -> null
            }
        }
}
