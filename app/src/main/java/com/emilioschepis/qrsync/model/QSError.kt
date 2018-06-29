package com.emilioschepis.qrsync.model

import android.support.annotation.StringRes
import com.emilioschepis.qrsync.R
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException

sealed class QSError(@StringRes val resId: Int, vararg val params: Any?) {

    sealed class AuthenticationError(@StringRes resId: Int) : QSError(resId) {
        object InvalidUser :
                AuthenticationError(R.string.error_authentication_invalid_user)

        object InvalidCredentials :
                AuthenticationError(R.string.error_authentication_invalid_credentials)

        object EmptyEmail :
                AuthenticationError(R.string.error_authentication_empty_email)

        object EmptyPassword :
                AuthenticationError(R.string.error_authentication_empty_password)

        object MalformedEmail :
                AuthenticationError(R.string.error_authentication_malformed_email)

        object WeakPassword :
                AuthenticationError(R.string.error_authentication_weak_password)

        object UserCollision :
                AuthenticationError(R.string.error_authentication_user_collision)
    }

    sealed class DatabaseError(@StringRes resId: Int) : QSError(resId) {
        object PermissionDenied :
                DatabaseError(R.string.error_firestore_permission_denied)

        object NotFound :
                DatabaseError(R.string.error_firestore_not_found)

        object AlreadyExists :
                DatabaseError(R.string.error_firestore_code_already_exist)
    }

    object NetworkError : QSError(R.string.error_network_unavailable)

    data class Unknown(val message: String?) : QSError(R.string.error_generic_unknown_reason, message)

    companion object {
        fun fromException(ex: Exception): QSError {
            return when (ex) {
                is FirebaseFirestoreException -> {
                    when (ex.code) {
                        FirebaseFirestoreException.Code.PERMISSION_DENIED,
                        FirebaseFirestoreException.Code.UNAUTHENTICATED ->
                            QSError.DatabaseError.PermissionDenied

                        FirebaseFirestoreException.Code.NOT_FOUND ->
                            QSError.DatabaseError.NotFound

                        FirebaseFirestoreException.Code.ALREADY_EXISTS ->
                            QSError.DatabaseError.AlreadyExists

                        FirebaseFirestoreException.Code.UNAVAILABLE ->
                            QSError.NetworkError

                        else ->
                            QSError.Unknown(ex.message)
                    }
                }
                is FirebaseAuthInvalidUserException ->
                    AuthenticationError.InvalidUser
                is FirebaseAuthInvalidCredentialsException -> {
                    // This is necessary because FirebaseAuthInvalidCredentialsException
                    // is thrown in sign in if the password is wrong
                    // and in sign up if the email is malformed
                    if (ex.message?.startsWith("The password is invalid", true) == true) {
                        AuthenticationError.InvalidCredentials
                    } else {
                        AuthenticationError.MalformedEmail
                    }
                }
                is FirebaseAuthUserCollisionException ->
                    AuthenticationError.UserCollision
                is FirebaseAuthWeakPasswordException ->
                    AuthenticationError.WeakPassword
                is FirebaseNetworkException ->
                    NetworkError
                else ->
                    QSError.Unknown(ex.message)

            }
        }
    }


    /*

    sealed class SignInError(@StringRes val resId: Int, vararg val params: Any?) : QSError() {
        data class Unknown(val message: String?) :
                SignInError(R.string.error_generic_unknown_reason, message)

        object InvalidUser :
                SignInError(R.string.error_authentication_invalid_user)

        object InvalidCredentials :
                SignInError(R.string.error_authentication_invalid_credentials)

        object EmptyEmail :
                SignInError(R.string.error_authentication_empty_email)

        object EmptyPassword :
                SignInError(R.string.error_authentication_empty_password)

        object MalformedEmail :
                SignInError(R.string.error_authentication_malformed_email)

        object WeakPassword :
                SignInError(R.string.error_authentication_weak_password)

    }

    sealed class SignUpError(@StringRes val resId: Int, vararg val params: Any?) : QSError() {
        data class Unknown(val message: String?) :
                SignUpError(R.string.error_generic_unknown_reason, message)

        object MalformedEmail :
                SignUpError(R.string.error_authentication_malformed_email)

        object WeakPassword :
                SignUpError(R.string.error_authentication_weak_password)

        object UserCollision :
                SignUpError(R.string.error_authentication_user_collision)

        object EmptyEmail :
                SignUpError(R.string.error_authentication_empty_email)

        object EmptyPassword :
                SignUpError(R.string.error_authentication_empty_password)
    }

    sealed class CollectionRetrievalError(@StringRes val resId: Int, vararg val params: Any?) : QSError() {
        data class Unknown(val message: String?) :
                CollectionRetrievalError(R.string.error_generic_unknown_reason, message)

        object PermissionDenied :
                CollectionRetrievalError(R.string.error_firestore_permission_denied)
    }

    sealed class CodeRetrievalError(@StringRes val resId: Int, vararg val params: Any?) : QSError() {
        data class Unknown(val message: String?) :
                CodeRetrievalError(R.string.error_generic_unknown_reason, message)

        object PermissionDenied :
                CodeRetrievalError(R.string.error_firestore_permission_denied)

        object NotFound :
                CodeRetrievalError(R.string.error_firestore_not_found)
    }

    sealed class InfoRetrievalError(@StringRes val resId: Int, vararg val params: Any?) : QSError() {
        data class Unknown(val message: String?) :
                InfoRetrievalError(R.string.error_generic_unknown_reason, message)

        object PermissionDenied :
                InfoRetrievalError(R.string.error_firestore_permission_denied)

        object NotFound :
                InfoRetrievalError(R.string.error_firestore_not_found)
    }

    sealed class CodeUploadError(@StringRes val resId: Int, vararg val params: Any?) : QSError() {
        data class Unknown(val message: String?) :
                CodeUploadError(R.string.error_generic_unknown_reason, message)

        object PermissionDenied :
                CodeUploadError(R.string.error_firestore_permission_denied)

        object AlreadyExists :
                CodeUploadError(R.string.error_firestore_code_already_exist)
    }

    sealed class CodeDeletionError(@StringRes val resId: Int, vararg val params: Any?) : QSError() {
        data class Unknown(val message: String?) :
                CodeDeletionError(R.string.error_generic_unknown_reason, message)

        object PermissionDenied :
                CodeDeletionError(R.string.error_firestore_permission_denied)

        object NotFound :
                CodeDeletionError(R.string.error_firestore_not_found)
    }

    sealed class CodeUpdateError(@StringRes val resId: Int, vararg val params: Any?) : QSError() {
        data class Unknown(val message: String?) :
                CodeUpdateError(R.string.error_generic_unknown_reason, message)

        object PermissionDenied :
                CodeUpdateError(R.string.error_firestore_permission_denied)

        object NotFound :
                CodeUpdateError(R.string.error_firestore_not_found)
    }

    sealed class ImageScanError(@StringRes val resId: Int, vararg val params: Any?) : QSError() {
        data class Unknown(val message: String?) :
                ImageScanError(R.string.error_generic_unknown_reason, message)
    }

    */
}
