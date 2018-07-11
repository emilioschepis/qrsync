package com.emilioschepis.qrsync.model

import android.support.annotation.DrawableRes
import com.emilioschepis.qrsync.R
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
    val formattedDate: String
        get() = DateFormat.getDateTimeInstance()
                .format(this.timestamp.toDate())

    @get:Exclude
    val typeIcon: Int
        @DrawableRes get() = when (type) {
            QSCode.CodeType.UNKNOWN -> R.drawable.ic_help_outline_black_24dp
            QSCode.CodeType.TEXT -> R.drawable.ic_short_text_black_24dp
            QSCode.CodeType.URL -> R.drawable.ic_link_black_24dp
            QSCode.CodeType.CONTACT -> R.drawable.ic_person_black_24dp
            QSCode.CodeType.EMAIL -> R.drawable.ic_email_black_24dp
            QSCode.CodeType.SMS -> R.drawable.ic_sms_black_24dp
            QSCode.CodeType.PHONE -> R.drawable.ic_phone_black_24dp
            QSCode.CodeType.CALENDAR -> R.drawable.ic_event_black_24dp
            QSCode.CodeType.PRODUCT -> R.drawable.ic_shopping_cart_black_24dp
            QSCode.CodeType.ISBN -> R.drawable.ic_book_black_24dp
        }

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