package com.emilioschepis.qrsync.repository

import android.arch.lifecycle.LiveData
import arrow.core.Either
import arrow.core.Option
import com.emilioschepis.qrsync.model.QSCode
import com.emilioschepis.qrsync.model.QSError

interface IFirestoreRepository {
    fun retrieveCollection():
            LiveData<Either<QSError, List<QSCode>>>

    fun retrieveCode(id: String):
            LiveData<Either<QSError, QSCode>>

    fun retrieveInfo():
            LiveData<Either<QSError, String>>

    fun uploadCodes(codes: List<QSCode>):
            LiveData<Option<QSError>>

    fun deleteCode(id: String):
            LiveData<Option<QSError>>

    fun updateCodeField(id: String, updatedValues: Pair<String, Any>):
            LiveData<Option<QSError>>

    fun deleteAllCodes():
            LiveData<Option<QSError>>
}