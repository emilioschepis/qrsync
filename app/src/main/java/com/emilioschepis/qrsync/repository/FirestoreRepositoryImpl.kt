package com.emilioschepis.qrsync.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import arrow.core.*
import com.emilioschepis.qrsync.model.QSCode
import com.emilioschepis.qrsync.model.QSError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FirestoreRepositoryImpl(private val firestore: FirebaseFirestore,
                              private val auth: FirebaseAuth) : IFirestoreRepository {

    private val currentUser: FirebaseUser?
        get() = auth.currentUser

    private val codesReference: CollectionReference
        get() {
            requireNotNull(currentUser)
            return firestore.collection("users/${currentUser!!.uid}/codes")
        }

    override fun retrieveInfo():
            LiveData<Either<QSError, String>> {
        val documentReference = firestore
                .collection("public")
                .document("app_info_panel")
        val observable = MutableLiveData<Either<QSError, String>>()

        documentReference.get()
                .addOnSuccessListener {
                    // The data is only acceptable if the document exists
                    // and its data is not empty
                    if (!it.exists() || it["info"].toString().isBlank()) {
                        observable.postValue(QSError.DatabaseError.NotFound.left())
                    } else {
                        // In order to have a multiline text, every \n is
                        // replaced with a new line
                        val info = it.data?.get("info").toString()
                                .replace("\\n", "\n")

                        observable.postValue(info.right())
                    }
                }
                .addOnFailureListener {
                    val error = QSError.fromException(it)
                    observable.postValue(error.left())
                }

        return observable
    }

    override fun retrieveCollection():
            LiveData<Either<QSError, List<QSCode>>> {
        val observable = MutableLiveData<Either<QSError, List<QSCode>>>()

        codesReference.orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // An error occurred.
                val error = QSError.fromException(exception)
                observable.postValue(error.left())
            } else {
                // The list might be null, in that case an
                // emptyList is automatically passed as the
                // result
                snapshot?.toObjects(QSCode::class.java)?.let {
                    observable.postValue(it.right())
                }
            }
        }
        return observable
    }

    override fun retrieveCode(id: String):
            LiveData<Either<QSError, QSCode>> {
        val codeReference = codesReference.document(id)
        val observable = MutableLiveData<Either<QSError, QSCode>>()

        codeReference.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // An error occurred.
                val error = QSError.fromException(exception)
                observable.postValue(error.left())
            } else {
                // The detail must not be null, that would
                // mean that the code was not found.
                val detail = snapshot?.toObject(QSCode::class.java)
                if (snapshot == null || detail == null) {
                    observable.postValue(QSError.DatabaseError.NotFound.left())
                } else {
                    observable.postValue(detail.right())
                }
            }
        }
        return observable
    }

    override fun uploadCodes(codes: List<QSCode>):
            LiveData<Option<QSError>> {

        val batch = firestore.batch()
        val observable = MutableLiveData<Option<QSError>>()

        codes.forEach {
            // Each code gets added to the batch, in order to
            // perform a batched write
            batch.set(codesReference.document(it.id), it)
        }

        batch.commit()
                .addOnSuccessListener {
                    observable.postValue(None)
                }
                .addOnFailureListener {
                    val error = QSError.fromException(it)
                    observable.postValue(error.some())
                }

        return observable
    }

    override fun updateCodeField(id: String, updatedValues: Pair<String, Any>):
            LiveData<Option<QSError>> {
        val codeReference = codesReference.document(id)
        val observable = MutableLiveData<Option<QSError>>()

        codeReference.update(mapOf(updatedValues))
                .addOnSuccessListener {
                    observable.postValue(None)
                }
                .addOnFailureListener {
                    val error = QSError.fromException(it)
                    observable.postValue(error.some())
                }

        return observable
    }

    override fun deleteCode(id: String):
            LiveData<Option<QSError>> {
        val codeReference = codesReference.document(id)
        val observable = MutableLiveData<Option<QSError>>()

        codeReference.delete()
                .addOnSuccessListener {
                    observable.postValue(None)
                }
                .addOnFailureListener {
                    val error = QSError.fromException(it)
                    observable.postValue(error.some())
                }

        return observable
    }

    override fun deleteAllCodes():
            LiveData<Option<QSError>> {
        return Transformations.switchMap(retrieveCollection()) {
            it.fold({
                MutableLiveData<Option<QSError>>().apply { postValue(it.some()) }
            }, {
                val batch = firestore.batch()
                val observable = MutableLiveData<Option<QSError>>()

                it.forEach {
                    batch.delete(codesReference.document(it.id))
                }

                batch.commit()
                        .addOnSuccessListener {
                            observable.postValue(None)
                        }
                        .addOnFailureListener {
                            val error = QSError.fromException(it)
                            observable.postValue(error.some())
                        }

                return@fold observable
            })
        }
    }
}