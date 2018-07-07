package com.emilioschepis.qrsync.extension

import android.app.Activity
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.text.InputType
import android.view.View
import android.widget.Toast
import com.emilioschepis.qrsync.model.QSError
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.alert

fun Activity.dialog(title: String? = null,
                    message: String) =
        this.alert(title = title, message = message) {
            okButton { }
        }


fun Fragment.dialog(title: String? = null,
                    message: String) =
        this.alert(title = title, message = message) {
            okButton { }
        }

fun Activity.confirmationDialog(title: String? = null,
                                message: String,
                                callback: () -> Unit) =
        this.alert(title = title, message = message) {
            okButton { callback.invoke() }
            cancelButton { }
        }

fun Fragment.confirmationDialog(title: String? = null,
                                message: String,
                                callback: () -> Unit) =
        this.alert(title = title, message = message) {
            okButton { callback.invoke() }
            cancelButton { }
        }

fun Activity.editableDialog(@StringRes title: Int,
                            start: String = "",
                            callback: (String) -> Unit) =
        alert {
            titleResource = title
            customView {
                val editText = editText {
                    maxLines = 1
                    inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                    setText(start)
                }
                okButton { callback.invoke(editText.text.toString()) }
            }
        }

fun Activity.snackbarMessage(root: View,
                             message: String,
                             duration: Int = Snackbar.LENGTH_SHORT) {
    doAsync {
        Snackbar.make(root, message, duration).show()
    }
}

fun Activity.snackbarError(root: View,
                           error: QSError,
                           duration: Int = Snackbar.LENGTH_INDEFINITE,
                           callback: (() -> Unit)? = null) {
    doAsync {
        val message = getString(error.resId, error.params.firstOrNull())
        Snackbar.make(root, message, duration)
                .apply { setAction(android.R.string.ok) { callback?.invoke() } }
                .show()
    }
}

fun Activity.toastMessage(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.toastError(error: QSError) {
    val message = getString(error.resId, error.params.firstOrNull())
    Toast.makeText(this.activity, message, Toast.LENGTH_SHORT).show()
}