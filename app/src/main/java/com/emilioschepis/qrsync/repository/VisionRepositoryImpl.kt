package com.emilioschepis.qrsync.repository

import android.arch.lifecycle.MutableLiveData
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.emilioschepis.qrsync.model.QSError
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata

class VisionRepositoryImpl(private val vision: FirebaseVision) : IVisionRepository {
    override fun scanImage(bytes: ByteArray, width: Int, height: Int): MutableLiveData<Either<QSError, List<FirebaseVisionBarcode>>> {
        val metadata = FirebaseVisionImageMetadata.Builder()
                .setWidth(width)
                .setHeight(height)
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .build()

        val fvi = FirebaseVisionImage.fromByteArray(bytes, metadata)
        val observable = MutableLiveData<Either<QSError, List<FirebaseVisionBarcode>>>()

        vision.getVisionBarcodeDetector(options).detectInImage(fvi)
                .addOnSuccessListener { observable.postValue(it.right()) }
                .addOnFailureListener { observable.postValue(QSError.fromException(it).left()) }

        return observable
    }

    companion object {
        // This might be needed if I decided to
        // limit the types of barcodes recognized.
        private val options = FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_ALL_FORMATS)
                .build()
    }
}