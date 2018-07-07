package com.emilioschepis.qrsync.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import java.text.DateFormat
import java.util.*

data class QSCode(val id: String = UUID.randomUUID().toString(),
                  val title: String = "",
                  val content: String = "",
                  val rawValue: String = "",
                  val extra: String = "",
                  val type: CodeType = CodeType.UNKNOWN,
                  val timestamp: Timestamp = Timestamp.now()) {

    @get:Exclude
    val formattedDate = DateFormat.getDateTimeInstance()
            .format(timestamp.toDate()) ?: ""

    enum class CodeType {
        UNKNOWN,
        TEXT,
        URL,
        CONTACT,
        EMAIL,
        SMS,
        PHONE,
        CALENDAR,
        PRODUCT,
        ISBN
    }
}