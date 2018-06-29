package com.emilioschepis.qrsync.repository

import android.arch.lifecycle.MutableLiveData
import arrow.core.Either
import arrow.core.Option
import arrow.core.left
import arrow.core.right
import com.emilioschepis.qrsync.model.QSError
import com.emilioschepis.qrsync.model.QSUser
import com.emilioschepis.qrsync.model.QSUserCredentials
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthRepositoryImpl(private val auth: FirebaseAuth) : IAuthRepository {

    override fun signOut() {
        auth.signOut()
    }

    override fun getCurrentUser(): Option<QSUser> {
        val uid = auth.currentUser?.uid
        val displayName = auth.currentUser?.displayName

        return if (uid == null) {
            Option.empty()
        } else {
            Option.just(QSUser(uid, displayName))
        }
    }

    override fun signInWithGoogle(credentials: GoogleSignInAccount):
            MutableLiveData<Either<QSError, QSUser>> {
        val observable = MutableLiveData<Either<QSError, QSUser>>()
        val token = credentials.idToken

        auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null))
                .addOnSuccessListener {
                    val firebaseUser = it.user

                    // Generate a QSUser instance with the received data
                    val user = QSUser(firebaseUser.uid, firebaseUser.displayName)

                    // Return the QSUser as a result
                    observable.postValue(user.right())
                }
                .addOnFailureListener {
                    val error = when (it) {
                        is FirebaseNetworkException ->
                            QSError.NetworkError

                        else -> QSError.Unknown(it.message)
                    }
                    observable.postValue(error.left())
                }

        return observable
    }

    override fun signIn(credentials: QSUserCredentials):
            MutableLiveData<Either<QSError, QSUser>> {
        require(credentials.email.isNotBlank())
        require(credentials.password.isNotBlank())

        val observable = MutableLiveData<Either<QSError, QSUser>>()

        auth.signInWithEmailAndPassword(credentials.email, credentials.password)
                .addOnSuccessListener {
                    val firebaseUser = it.user

                    // Generate a QSUser instance with the received data
                    val user = QSUser(firebaseUser.uid, firebaseUser.displayName)

                    // Return the QSUser as a result
                    observable.postValue(user.right())
                }
                .addOnFailureListener {
                    val error = QSError.fromException(it)
                    observable.postValue(error.left())
                }

        return observable
    }

    override fun signUp(credentials: QSUserCredentials):
            MutableLiveData<Either<QSError, QSUser>> {
        require(credentials.email.isNotBlank())
        require(credentials.password.isNotBlank())

        val observable = MutableLiveData<Either<QSError, QSUser>>()

        auth.createUserWithEmailAndPassword(credentials.email, credentials.password)
                .addOnSuccessListener {
                    val firebaseUser = it.user

                    // Generate a QSUser instance with the received data
                    val user = QSUser(firebaseUser.uid, firebaseUser.displayName)

                    // Return the QSUser as a result
                    observable.postValue(user.right())
                }
                .addOnFailureListener {
                    val error = QSError.fromException(it)
                    observable.postValue(error.left())
                }

        return observable
    }
}