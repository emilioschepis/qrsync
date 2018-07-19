package com.emilioschepis.qrsync.ui.preferences

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import arrow.core.Option
import com.emilioschepis.qrsync.model.QSError
import com.emilioschepis.qrsync.repository.IAuthRepository
import com.emilioschepis.qrsync.repository.IConfigRepository
import com.emilioschepis.qrsync.repository.IFirestoreRepository

class PreferencesViewModel(private val auth: IAuthRepository,
                           private val firestore: IFirestoreRepository,
                           config: IConfigRepository) : ViewModel() {
    fun signOut() {
        auth.signOut()
    }

    fun deleteAllCodes(): LiveData<Option<QSError>> {
        return Transformations.map(firestore.deleteAllCodes()) { it }
    }

    val websiteUrl = config.websiteUrl
}