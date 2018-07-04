package com.emilioschepis.qrsync.model

import android.arch.lifecycle.LiveData
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.google.firebase.firestore.*

class QSCodeListLiveData(private val reference: CollectionReference) :
        LiveData<Either<QSError, List<QSCode>>>(), EventListener<QuerySnapshot> {

    private var registration: ListenerRegistration? = null

    override fun onEvent(snapshot: QuerySnapshot?, exception: FirebaseFirestoreException?) {
        if (exception != null) {
            // An error occurred.
            val error = QSError.fromException(exception)
            this.value = error.left()
        } else {
            // The list might be null, in that case an
            // emptyList is automatically passed as the
            // result
            val detail = snapshot?.toObjects(QSCode::class.java)
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
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(this)
    }

    override fun onInactive() {
        registration?.remove()
        registration = null
        super.onInactive()
    }
}