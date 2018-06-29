package com.emilioschepis.qrsync.ui.scan

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import arrow.core.Either
import arrow.core.Option
import com.emilioschepis.qrsync.model.QSCode
import com.emilioschepis.qrsync.model.QSError
import com.emilioschepis.qrsync.repository.IFirestoreRepository
import com.emilioschepis.qrsync.repository.IVisionRepository
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode.TYPE_ISBN
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode.TYPE_PRODUCT
import com.google.gson.Gson

class ScanViewModel(private val vision: IVisionRepository,
                    private val firestore: IFirestoreRepository) : ViewModel() {

    fun scanImage(bytes: ByteArray, width: Int, height: Int): LiveData<Either<QSError, List<FirebaseVisionBarcode>>> {
        return Transformations.map(vision.scanImage(bytes, width, height)) { it }
    }

    fun uploadCodes(barcodes: List<FirebaseVisionBarcode>): LiveData<Option<QSError>> {
        val codes = barcodes.map { it.toCode() }
        return Transformations.map(firestore.uploadCodes(codes)) { it }
    }

    private fun FirebaseVisionBarcode.toCode(): QSCode {
        val content = this.displayValue.orEmpty()
        val rawValue = this.rawValue.orEmpty()

        val type = when (this.valueType) {
            FirebaseVisionBarcode.TYPE_TEXT -> QSCode.CodeType.TEXT
            FirebaseVisionBarcode.TYPE_PHONE -> QSCode.CodeType.PHONE
            FirebaseVisionBarcode.TYPE_URL -> QSCode.CodeType.URL
            FirebaseVisionBarcode.TYPE_EMAIL -> QSCode.CodeType.EMAIL
            FirebaseVisionBarcode.TYPE_CONTACT_INFO -> QSCode.CodeType.CONTACT
            FirebaseVisionBarcode.TYPE_CALENDAR_EVENT -> QSCode.CodeType.CALENDAR
            FirebaseVisionBarcode.TYPE_SMS -> QSCode.CodeType.SMS
            FirebaseVisionBarcode.TYPE_PRODUCT -> QSCode.CodeType.PRODUCT
            FirebaseVisionBarcode.TYPE_ISBN -> QSCode.CodeType.ISBN
            else -> QSCode.CodeType.UNKNOWN
        }

        // The code's extra portion is converted to Json
        val extra = Gson().toJson(when (this.valueType) {
            FirebaseVisionBarcode.TYPE_TEXT, TYPE_PRODUCT, TYPE_ISBN -> displayValue
            FirebaseVisionBarcode.TYPE_PHONE -> phone
            FirebaseVisionBarcode.TYPE_URL -> url
            FirebaseVisionBarcode.TYPE_EMAIL -> email
            FirebaseVisionBarcode.TYPE_CONTACT_INFO -> contactInfo
            FirebaseVisionBarcode.TYPE_CALENDAR_EVENT -> calendarEvent
            FirebaseVisionBarcode.TYPE_SMS -> sms
            else -> ""
        })

        return QSCode(content = content, rawValue = rawValue, type = type, extra = extra)
    }
}