package com.emilioschepis.qrsync.ui.preferences

import android.arch.lifecycle.Observer
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v14.preference.SwitchPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.widget.Toast
import com.emilioschepis.qrsync.R
import com.emilioschepis.qrsync.extension.confirmationDialog
import com.emilioschepis.qrsync.extension.toastError
import com.emilioschepis.qrsync.model.QSError
import com.emilioschepis.qrsync.ui.about.AboutActivity
import com.emilioschepis.qrsync.ui.splash.SplashActivity
import com.google.firebase.iid.FirebaseInstanceId
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.IOException

class PreferencesFragment : PreferenceFragmentCompat() {

    private val viewModel by viewModel<PreferencesViewModel>()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)

        // When an user attempts to change the FCM preference
        // we need to inform them that an FCM ID might be saved
        findPreference("key_fcm_permission").onPreferenceChangeListener = Preference.OnPreferenceChangeListener { pref, newValue ->
            assert(newValue is Boolean)
            assert(pref is SwitchPreference)

            val preference = pref as SwitchPreference

            // If the user is disabling push notifications
            // we immediately do so
            // If the user is enabling them, we show them a dialog
            // explaining that an Instance ID might be saved
            if (newValue == false) {
                preference.isChecked = false

                // Firebase Instance ID deletion has to be called outside
                // of the main thread
                doAsync {
                    try {
                        FirebaseInstanceId.getInstance().deleteInstanceId()
                    } catch (ex: IOException) {
                        uiThread {
                            toastError(QSError.Unknown(ex.message))
                        }
                    }
                }
            } else {
                confirmationDialog(message = getString(R.string.info_enabling_fcm)) {
                    preference.isChecked = true

                    // Generating a new ID is necessary as the autoinit
                    // property is permanently set to false
                    FirebaseInstanceId.getInstance().instanceId
                }.show()
            }

            return@OnPreferenceChangeListener false
        }
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            "key_sign_out" -> {
                confirmationDialog(message = getString(R.string.question_sign_out)) {
                    viewModel.signOut()
                    startActivity(Intent(activity, SplashActivity::class.java))
                    activity?.finishAffinity()
                }.show()
            }
            "key_delete_all_codes" -> {
                confirmationDialog(message = getString(R.string.question_delete_all_codes)) {
                    viewModel.deleteAllCodes().observe(this, Observer {
                        it?.fold(this::onCodesDeletionSuccess, this::onCodesDeletionError)
                    })
                }.show()
            }
            "key_website" -> {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW).apply { data = viewModel.websiteUrl })
                } catch (ex: ActivityNotFoundException) {
                    Toast.makeText(this.activity, R.string.error_activity_not_found, Toast.LENGTH_SHORT).show()
                }
            }
            "key_info" -> {
                startActivity(Intent(activity, AboutActivity::class.java))
            }
            "key_feedback" -> {
                openFeedbackEmail()
            }
        }

        return super.onPreferenceTreeClick(preference)
    }

    private fun openFeedbackEmail() {
        val i = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:emilio.schepis@gmail.com")
            putExtra(Intent.EXTRA_EMAIL, "emilio.schepis@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, "QR Sync - Feedback")
        }

        try {
            startActivity(i)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(this.activity, R.string.error_activity_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    private fun onCodesDeletionError(error: QSError) {
        toastError(error)
    }

    private fun onCodesDeletionSuccess() {
        activity?.finish()
    }
}