package com.emilioschepis.qrsync.ui.preferences

import android.app.AlertDialog
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
import com.emilioschepis.qrsync.model.QSError
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
                            showMessage(getString(R.string.error_generic_unknown_reason, ex.message))
                        }
                    }
                }
            } else {
                showConfirmationDialog(getString(R.string.info_enabling_fcm)) {
                    preference.isChecked = true

                    // Generating a new ID is necessary as the autoinit
                    // property is permanently set to false
                    FirebaseInstanceId.getInstance().instanceId
                }
            }

            return@OnPreferenceChangeListener false
        }
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            "key_sign_out" -> {
                showConfirmationDialog(getString(R.string.question_sign_out)) {
                    viewModel.signOut()
                    activity?.finishAffinity()
                }
            }
            "key_delete_all_codes" -> {
                showConfirmationDialog(getString(R.string.question_delete_all_codes)) {
                    viewModel.deleteAllCodes().observe(this, Observer {
                        it?.fold(this::onCodesDeletionSuccess, this::onCodesDeletionError)
                    })
                }
            }
            "key_info" -> {
                viewModel.retrieveInfo().observe(this, Observer {
                    it?.fold(this::onInfoRetrievalError, this::onInfoRetrievalSuccess)
                })
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
            showMessage(getString(R.string.error_activity_not_found), Toast.LENGTH_SHORT)
        }
    }

    private fun onCodesDeletionError(error: QSError) {
        showMessage(getString(error.resId, error.params.getOrNull(0)))
    }

    private fun onCodesDeletionSuccess() {
        activity?.finish()
    }

    private fun onInfoRetrievalError(error: QSError) {
        showMessage(getString(error.resId, error.params.getOrNull(0)))
    }

    private fun onInfoRetrievalSuccess(info: String) {
        showDialog(info)
    }

    private fun showDialog(message: String) {
        AlertDialog.Builder(activity)
                .setCancelable(true)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
    }

    private fun showConfirmationDialog(message: String, callback: () -> Unit) {
        AlertDialog.Builder(activity)
                .setCancelable(true)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    callback.invoke()
                    dialog.dismiss()
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
    }

    private fun showMessage(message: String, duration: Int = Toast.LENGTH_LONG) {
        Toast.makeText(activity, message, duration).show()
    }
}