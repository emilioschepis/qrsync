package com.emilioschepis.qrsync.extension

import android.app.Activity
import android.support.v4.app.Fragment
import android.text.InputType
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


fun Activity.editableDialog(title: String? = null,
                            start: String = "",
                            callback: (String) -> Unit) =
        alert(title = title, message = "") {
            customView {
                verticalLayout {
                    setPadding(24, 0, 24, 0)
                    val editText = editText {
                        maxLines = 1
                        inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                        setText(start)
                    }
                    okButton { callback.invoke(editText.text.toString()) }
                }
            }

        }