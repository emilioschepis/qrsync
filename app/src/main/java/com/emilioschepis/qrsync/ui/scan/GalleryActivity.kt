package com.emilioschepis.qrsync.ui.scan

import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.emilioschepis.qrsync.R
import com.emilioschepis.qrsync.extension.toastError
import com.emilioschepis.qrsync.model.QSError
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import org.jetbrains.anko.toast
import org.koin.android.viewmodel.ext.android.viewModel
import android.provider.MediaStore



class GalleryActivity : AppCompatActivity() {

    private val viewModel by viewModel<ScanViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.prompt_select_image)), 1001)
        } catch (ex: ActivityNotFoundException) {
            toastError(QSError.fromException(ex))
            finish()
        }
    }

    private fun uploadCodes(barcodes: List<FirebaseVisionBarcode>) {
        viewModel.uploadCodes(barcodes).observe(this, Observer {
            it?.fold(this::onUploadSuccess, this::onUploadError)
        })
    }

    private fun onImageScanError(error: QSError) {
        toastError(error)
        finish()
    }

    private fun onImageScanSuccess(barcodes: List<FirebaseVisionBarcode>) {
        if (barcodes.isEmpty()) {
            toast(R.string.info_no_codes)
            finish()
        } else {
            uploadCodes(barcodes)
        }
    }

    private fun onUploadError(error: QSError) {
        toastError(error)
        finish()
    }

    private fun onUploadSuccess() {
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            1001 -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data?.data != null){
                        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, data.data)
                        viewModel.scanImage(bitmap).observe(this, Observer {
                            it?.fold(this::onImageScanError, this::onImageScanSuccess)
                        })
                    } else {
                        toast(R.string.info_no_codes)
                        finish()
                    }
                } else {
                    finish()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
