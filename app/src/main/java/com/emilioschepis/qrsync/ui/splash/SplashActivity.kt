package com.emilioschepis.qrsync.ui.splash

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.emilioschepis.qrsync.R
import com.emilioschepis.qrsync.ui.codelist.CodeListActivity
import com.emilioschepis.qrsync.ui.signin.SignInActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import org.koin.android.architecture.ext.viewModel

class SplashActivity : AppCompatActivity() {

    private val viewModel by viewModel<SplashViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (GoogleApiAvailability.getInstance()
                        .isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            Toast.makeText(this@SplashActivity,
                    R.string.error_play_services_required, Toast.LENGTH_LONG)
                    .show()
            return
        }

        viewModel.authentication.observe(this, Observer {
            it?.fold(this::onUserSignedOut) { onUserSignedIn() }
        })
    }

    private fun onUserSignedOut() {
        startActivity(Intent(this, SignInActivity::class.java))
        finishAffinity()
    }

    private fun onUserSignedIn() {
        startActivity(Intent(this, CodeListActivity::class.java))
        finishAffinity()
    }
}
