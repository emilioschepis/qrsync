package com.emilioschepis.qrsync.ui.signin

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import arrow.core.Either
import arrow.core.left
import arrow.data.*
import com.emilioschepis.qrsync.model.QSError
import com.emilioschepis.qrsync.model.QSUser
import com.emilioschepis.qrsync.model.QSUserCredentials
import com.emilioschepis.qrsync.repository.IAuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import java.util.regex.Pattern

class SignInViewModel(private val auth: IAuthRepository) : ViewModel() {

    private val mutableLoading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = mutableLoading

    fun authenticate(email: String, password: String): LiveData<Either<QSError, QSUser>> {
        return validateData(email, password).fold({
            Transformations.map(MutableLiveData<Either<QSError, QSUser>>().apply { value = it.head.left() }) { it }
        }, {
            mutableLoading.value = true
            return@fold Transformations.map(auth.signIn(it)) {
                mutableLoading.value = false
                return@map it
            }
        })
    }

    fun authenticateWithGoogle(credentials: GoogleSignInAccount): LiveData<Either<QSError, QSUser>> {
        mutableLoading.value = true
        return Transformations.map(auth.signInWithGoogle(credentials)) {
            mutableLoading.value = false
            it
        }
    }

    private fun validateData(email: String, password: String): Validated<Nel<QSError>, QSUserCredentials> {
        // A validated variable is created by combining the
        // validation status of the first two
        return Validated.applicative<Nel<QSError.AuthenticationError>>(Nel.semigroup())
                .map(email.validatedEmail(), password.validatedPassword()) {
                    QSUserCredentials(it.a, it.b)
                }.fix()
    }

    private fun String.validatedEmail(): Validated<Nel<QSError.AuthenticationError>, String> {
        val regex = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE)!!

        return if (this.isBlank()) {
            QSError.AuthenticationError.EmptyEmail.invalidNel()
        } else if (!regex.matcher(this).find()) {
            QSError.AuthenticationError.MalformedEmail.invalidNel()
        } else {
            this.valid()
        }
    }

    private fun String.validatedPassword(): Validated<Nel<QSError.AuthenticationError>, String> {
        val minimumSize = 6

        return when {
            this.isBlank() -> QSError.AuthenticationError.EmptyPassword.invalidNel()
            this.length < minimumSize -> QSError.AuthenticationError.WeakPassword.invalidNel()
            else -> this.valid()
        }
    }
}