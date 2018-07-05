package com.emilioschepis.qrsync.model

import android.arch.lifecycle.LiveData
import android.os.Handler
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.google.firebase.firestore.*
import org.jetbrains.anko.doAsync
import java.util.concurrent.atomic.AtomicBoolean

class QSCodeListLiveData(private val reference: CollectionReference) :
        LiveData<Either<QSError, List<QSCode>>>(), EventListener<QuerySnapshot> {

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

    override fun onEvent(snapshot: QuerySnapshot?, exception: FirebaseFirestoreException?) {
        doAsync {
            if (exception != null) {
                // An error occurred.
                val error = QSError.fromException(exception)
                this@QSCodeListLiveData.postValue(error.left())
            } else {
                // The list might be null, in that case an
                // emptyList is automatically passed as the
                // result
                val detail = snapshot?.toObjects(QSCode::class.java)
                if (snapshot == null || detail == null) {
                    val error = QSError.DatabaseError.NotFound
                    this@QSCodeListLiveData.postValue(error.left())
                } else {
                    this@QSCodeListLiveData.postValue(detail.right())
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
                    .orderBy("timestamp", Query.Direction.DESCENDING)
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