package com.emilioschepis.qrsync.model

import android.arch.lifecycle.LiveData
import android.os.Handler
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.google.firebase.firestore.*
import org.jetbrains.anko.doAsync
import java.util.concurrent.atomic.AtomicBoolean

class QSCodeLiveData(private val reference: DocumentReference) :
        LiveData<Either<QSError, QSCode>>(), EventListener<DocumentSnapshot> {

    companion object {
        private const val DELAY = 2000L
    }

    private var registration: ListenerRegistration? = null

    private val pending = AtomicBoolean(false)
    private val handler = Handler()
    private val removeListener = Runnable {
        registration?.remove()
        registration = null
        pending.compareAndSet(true, false)
    }

    override fun onEvent(snapshot: DocumentSnapshot?, exception: FirebaseFirestoreException?) {
        doAsync {
            if (exception != null) {
                // An error occurred.
                val error = QSError.fromException(exception)
                this@QSCodeLiveData.postValue(error.left())
            } else {
                // The detail must not be null, that would
                // mean that the code was not found.
                val detail = snapshot?.toObject(QSCode::class.java)
                if (snapshot == null || detail == null) {
                    val error = QSError.DatabaseError.NotFound
                    this@QSCodeLiveData.postValue(error.left())
                } else {
                    this@QSCodeLiveData.postValue(detail.right())
                }
            }
        }
    }

    override fun onActive() {
        super.onActive()
        // If the LiveData becomes active and the old
        // listener is still attached, we stop the removal
        // and resume using the old one.
        // If the LiveData does not find the old listener it
        // creates a new one.
        if (pending.get()) {
            handler.removeCallbacks(removeListener)
        } else {
            registration = reference
                    .addSnapshotListener(this)
        }
        pending.set(false)
    }

    override fun onInactive() {
        handler.postDelayed(removeListener, DELAY)
        pending.compareAndSet(false, true)
        super.onInactive()
    }
}