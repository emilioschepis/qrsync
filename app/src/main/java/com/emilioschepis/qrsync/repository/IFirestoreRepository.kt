package com.emilioschepis.qrsync.repository

import android.arch.lifecycle.MutableLiveData
import arrow.core.Either
import arrow.core.Option
import com.emilioschepis.qrsync.model.QSCode
import com.emilioschepis.qrsync.model.QSError

interface IFirestoreRepository {
    fun retrieveCollection():
            MutableLiveData<Either<QSError, List<QSCode>>>

    fun retrieveCode(id: String):
            MutableLiveData<Either<QSError, QSCode>>

    fun retrieveInfo():
            MutableLiveData<Either<QSError, String>>

    fun uploadCodes(codes: List<QSCode>):
            MutableLiveData<Option<QSError>>

    fun deleteCode(id: String):
            MutableLiveData<Option<QSError>>

    fun updateCodeField(id: String, updatedValues: Pair<String, Any>):
            MutableLiveData<Option<QSError>>
}