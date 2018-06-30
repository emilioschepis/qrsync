package com.emilioschepis.qrsync.repository

import android.arch.lifecycle.LiveData
import arrow.core.Either
import com.emilioschepis.qrsync.model.QSError
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode

interface IVisionRepository {
    fun scanImage(bytes: ByteArray, width: Int, height: Int):
            LiveData<Either<QSError, List<FirebaseVisionBarcode>>>
}