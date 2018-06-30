package com.emilioschepis.qrsync.repository

import android.arch.lifecycle.LiveData
import arrow.core.Either
import arrow.core.Option
import com.emilioschepis.qrsync.model.QSError
import com.emilioschepis.qrsync.model.QSUser
import com.emilioschepis.qrsync.model.QSUserCredentials
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

interface IAuthRepository {
    fun getCurrentUser(): Option<QSUser>
    fun signIn(credentials: QSUserCredentials):
            LiveData<Either<QSError, QSUser>>

    fun signInWithGoogle(credentials: GoogleSignInAccount):
            LiveData<Either<QSError, QSUser>>

    fun signUp(credentials: QSUserCredentials):
            LiveData<Either<QSError, QSUser>>

    fun signOut()
}