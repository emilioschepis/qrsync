package com.emilioschepis.qrsync.model

import android.arch.lifecycle.LiveData
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.google.firebase.firestore.*

class QSCodeLiveData(private val reference: DocumentReference) :
        LiveData<Either<QSError, QSCode>>(), EventListener<DocumentSnapshot> {

    private var registration: ListenerRegistration? = null

    override fun onEvent(snapshot: DocumentSnapshot?, exception: FirebaseFirestoreException?) {
        if (exception != null) {
            // An error occurred.
            val error = QSError.fromException(exception)
            this.value = error.left()
        } else {
            // The detail must not be null, that would
            // mean that the code was not found.
            val detail = snapshot?.toObject(QSCode::class.java)
            if (snapshot == null || detail == null) {
                this.value = QSError.DatabaseError.NotFound.left()
            } else {
                this.value = detail.right()
            }
        }
    }

    override fun onActive() {
        super.onActive()
        registration = reference
                .addSnapshotListener(this)
    }

    override fun onInactive() {
        registration?.remove()
        registration = null
        super.onInactive()
    }
}