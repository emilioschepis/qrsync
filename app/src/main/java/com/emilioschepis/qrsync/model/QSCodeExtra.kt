package com.emilioschepis.qrsync.model

import android.provider.ContactsContract.CommonDataKinds.Phone.*
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode

sealed class QSCodeExtra {
    data class ContactPhone(val number: String, val type: Int) : QSCodeExtra() {
        companion object {
            fun fromFirebasePhone(source: FirebaseVisionBarcode.Phone): ContactPhone {
                val number = source.number.orEmpty()
                val type = when (source.type) {
                    FirebaseVisionBarcode.Phone.TYPE_HOME -> TYPE_HOME
                    FirebaseVisionBarcode.Phone.TYPE_WORK -> TYPE_WORK
                    FirebaseVisionBarcode.Phone.TYPE_MOBILE -> TYPE_MOBILE
                    FirebaseVisionBarcode.Phone.TYPE_FAX -> TYPE_FAX_WORK
                    else -> TYPE_OTHER
                }

                return ContactPhone(number, type)
            }
        }
    }

    data class ContactEmail(val address: String, val type: Int) {
        companion object {
            fun fromFirebaseEmail(source: FirebaseVisionBarcode.Email): ContactEmail {
                val address = source.address.orEmpty()
                val type = when (source.type) {
                    FirebaseVisionBarcode.Email.TYPE_HOME -> TYPE_HOME
                    FirebaseVisionBarcode.Email.TYPE_WORK -> TYPE_WORK
                    else -> TYPE_OTHER
                }

                return ContactEmail(address, type)
            }
        }
    }

    data class ContactAddress(val address: String, val type: Int) {
        companion object {
            fun fromFirebaseAddress(source: FirebaseVisionBarcode.Address): ContactAddress {
                val address = source.addressLines.joinToString(" ")
                val type = when (source.type) {
                    FirebaseVisionBarcode.Address.TYPE_HOME -> TYPE_HOME
                    FirebaseVisionBarcode.Address.TYPE_WORK -> TYPE_WORK
                    else -> TYPE_OTHER
                }

                return ContactAddress(address, type)
            }
        }
    }
}