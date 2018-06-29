package com.emilioschepis.qrsync.ui.detail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import arrow.core.Either
import arrow.core.Option
import com.emilioschepis.qrsync.model.QSCode
import com.emilioschepis.qrsync.model.QSCodeAction
import com.emilioschepis.qrsync.model.QSError
import com.emilioschepis.qrsync.repository.IFirestoreRepository


class DetailViewModel(private val id: String,
                      private val firestore: IFirestoreRepository) : ViewModel() {

    private val mutableLoading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = mutableLoading

    val code: LiveData<Either<QSError, QSCode>>
        get() {
            mutableLoading.value = true
            return Transformations.map(firestore.retrieveCode(id)) {
                mutableLoading.value = false
                return@map it
            }
        }

    val actions: LiveData<List<QSCodeAction>> =
            Transformations.map(code) {
                it.fold({
                    emptyList<QSCodeAction>()
                }, {
                    determineActions(it)
                })
            }

    var currentCode: QSCode = QSCode()

    fun deleteCode(): LiveData<Option<QSError>> {
        mutableLoading.value = true
        return Transformations.map(firestore.deleteCode(id)) {
            mutableLoading.value = false
            return@map it
        }
    }

    fun editTitle(newTitle: String): LiveData<Option<QSError>> {
        mutableLoading.value = true
        return Transformations.map(firestore
                .updateCodeField(id, "title" to newTitle)) {
            mutableLoading.value = false
            return@map it
        }
    }

    private fun determineActions(code: QSCode): List<QSCodeAction> {
        val actions = ArrayList<QSCodeAction>()

        actions.add(QSCodeAction.Delete)
        actions.add(QSCodeAction.EditTitle)
        actions.add(QSCodeAction.CopyContent)
        actions.add(QSCodeAction.ReadInfo)

        when (code.type) {
            QSCode.CodeType.TEXT ->
                actions.add(QSCodeAction.SearchGoogle)
            QSCode.CodeType.ISBN ->
                actions.add(QSCodeAction.FindBook)
            QSCode.CodeType.PRODUCT ->
                actions.add(QSCodeAction.FindProduct)
            QSCode.CodeType.CALENDAR ->
                actions.add(QSCodeAction.AddCalendarEvent)
            QSCode.CodeType.CONTACT ->
                actions.add(QSCodeAction.AddContact)
            QSCode.CodeType.EMAIL ->
                actions.add(QSCodeAction.SendEmail)
            QSCode.CodeType.SMS ->
                actions.add(QSCodeAction.SendSms)
            QSCode.CodeType.PHONE ->
                actions.add(QSCodeAction.CallPhone)
            QSCode.CodeType.URL ->
                actions.add(QSCodeAction.OpenUrl)
            QSCode.CodeType.UNKNOWN -> {
            }
        }

        // Return the actions in a reversed priority
        return actions.sortedBy { it.priority }.reversed()
    }
}