package com.emilioschepis.qrsync.model

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.emilioschepis.qrsync.R
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.gson.Gson
import java.util.*

sealed class QSCodeAction(val priority: Int, @StringRes val name: Int, @DrawableRes val icon: Int, val intent: ((QSCode) -> Intent)? = null) {

    object Delete : QSCodeAction(0, R.string.action_delete, R.drawable.ic_delete_black_24dp)
    object EditTitle : QSCodeAction(2, R.string.action_edit_title, R.drawable.ic_title_black_24dp)
    object CopyContent : QSCodeAction(3, R.string.action_copy_content, R.drawable.ic_content_copy_black_24dp)
    object ReadInfo : QSCodeAction(1, R.string.action_read_info, R.drawable.ic_info_black_24dp)

    object FindProduct : QSCodeAction(4, R.string.action_find_product, R.drawable.ic_shopping_cart_black_24dp, {
        require(it.type == QSCode.CodeType.PRODUCT)

        // Replace both whitespaces and newlines with +
        val searchQuery = it.content
        val searchUri = Uri.parse("http://www.google.com/search?q=$searchQuery")
        Intent(Intent.ACTION_VIEW).apply { data = searchUri }

    })

    object FindBook : QSCodeAction(4, R.string.action_find_book, R.drawable.ic_book_black_24dp, {
        require(it.type == QSCode.CodeType.ISBN)

        // Replace both whitespaces and newlines with +
        val searchQuery = it.content
        val searchUri = Uri.parse("http://www.google.com/search?q=$searchQuery")
        Intent(Intent.ACTION_VIEW).apply { data = searchUri }
    })

    object SearchGoogle : QSCodeAction(4, R.string.action_search_google, R.drawable.ic_search, {
        require(it.type == QSCode.CodeType.TEXT)

        // Replace both whitespaces and newlines with +
        val searchQuery = it.content
                .replace(" ", "+")
                .replace("\n", "+")
        val searchUri = Uri.parse("http://www.google.com/search?q=$searchQuery")
        Intent(Intent.ACTION_VIEW).apply { data = searchUri }
    })

    object OpenUrl : QSCodeAction(4, R.string.action_open_url, R.drawable.ic_link_black_24dp, {
        require(it.type == QSCode.CodeType.URL)
        val url = if (it.content.startsWith("https://") ||
                it.content.startsWith("http://")) {
            it.content
        } else {
            "https://${it.content}"
        }
        Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(url) }
    })

    object AddContact : QSCodeAction(4, R.string.action_add_contact, R.drawable.ic_person_add_black_24dp, {
        assert(it.type == QSCode.CodeType.CONTACT)

        val contactInfo = Gson().fromJson(it.extra, FirebaseVisionBarcode.ContactInfo::class.java)

        val phones = contactInfo.phones.map(QSCodeExtra.ContactPhone.Companion::fromFirebasePhone)
        val emails = contactInfo.emails.map(QSCodeExtra.ContactEmail.Companion::fromFirebaseEmail)
        val address = contactInfo.addresses.map(QSCodeExtra.ContactAddress.Companion::fromFirebaseAddress).getOrNull(0)

        Intent(ContactsContract.Intents.Insert.ACTION).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE
            putExtra(ContactsContract.Intents.Insert.NAME, contactInfo.name?.formattedName)
            putExtra(ContactsContract.Intents.Insert.PHONETIC_NAME, contactInfo.name?.pronunciation)

            // Phones
            putExtra(ContactsContract.Intents.Insert.PHONE, phones.getOrNull(0)?.number)
            putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, phones.getOrNull(0)?.type)
            putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, phones.getOrNull(1)?.number)
            putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE, phones.getOrNull(1)?.type)
            putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE, phones.getOrNull(2)?.number)
            putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE_TYPE, phones.getOrNull(2)?.type)

            // Emails
            putExtra(ContactsContract.Intents.Insert.EMAIL, emails.getOrNull(0)?.address)
            putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, emails.getOrNull(0)?.type)
            putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL, emails.getOrNull(1)?.address)
            putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL_TYPE, emails.getOrNull(1)?.type)
            putExtra(ContactsContract.Intents.Insert.TERTIARY_EMAIL, emails.getOrNull(2)?.address)
            putExtra(ContactsContract.Intents.Insert.TERTIARY_EMAIL_TYPE, emails.getOrNull(2)?.type)

            // Address
            putExtra(ContactsContract.Intents.Insert.POSTAL, address?.address)
            putExtra(ContactsContract.Intents.Insert.POSTAL_TYPE, address?.type)
        }
    })

    object SendSms : QSCodeAction(4, R.string.action_send_sms, R.drawable.ic_sms_black_24dp, {
        assert(it.type == QSCode.CodeType.SMS)
        val sms = Gson().fromJson(it.extra,
                FirebaseVisionBarcode.Sms::class.java)

        Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${sms.phoneNumber}")
            putExtra("address", sms.phoneNumber)
            putExtra("sms_body", sms.message)
        }
    })

    object SendEmail : QSCodeAction(4, R.string.action_send_email, R.drawable.ic_email_black_24dp, {
        assert(it.type == QSCode.CodeType.EMAIL)

        val email = Gson().fromJson(it.extra,
                FirebaseVisionBarcode.Email::class.java)

        Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${email.address}")
            putExtra(Intent.EXTRA_EMAIL, email.address)
            putExtra(Intent.EXTRA_SUBJECT, email.subject)
            putExtra(Intent.EXTRA_TEXT, email.body)
        }
    })

    object CallPhone : QSCodeAction(4, R.string.action_call_phone, R.drawable.ic_phone_black_24dp, {
        assert(it.type == QSCode.CodeType.PHONE)

        val phone = Gson().fromJson(it.extra,
                FirebaseVisionBarcode.Phone::class.java)

        Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${phone.number}")
        }
    })

    object AddCalendarEvent : QSCodeAction(4, R.string.action_add_event, R.drawable.ic_event_black_24dp, {
        assert(it.type == QSCode.CodeType.CALENDAR)

        val calendarEvent = Gson().fromJson(it.extra,
                FirebaseVisionBarcode.CalendarEvent::class.java)

        val firebaseStartTime = calendarEvent.start
        val firebaseEndTime = calendarEvent.end

        val startTime = Calendar.getInstance().apply {
            firebaseStartTime?.let {
                set(Calendar.YEAR, it.year)
                set(Calendar.MONTH, it.month)
                set(Calendar.DAY_OF_MONTH, it.day)
                set(Calendar.HOUR_OF_DAY, it.hours)
                set(Calendar.MINUTE, it.minutes)
                set(Calendar.SECOND, it.seconds)
            }
        }

        val endTime = Calendar.getInstance().apply {
            firebaseEndTime?.let {
                set(Calendar.YEAR, it.year)
                set(Calendar.MONTH, it.month)
                set(Calendar.DAY_OF_MONTH, it.day)
                set(Calendar.HOUR_OF_DAY, it.hours)
                set(Calendar.MINUTE, it.minutes)
                set(Calendar.SECOND, it.seconds)
            }
        }

        Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, calendarEvent.summary)
            putExtra(CalendarContract.Events.DESCRIPTION, calendarEvent.description)
            putExtra(CalendarContract.Events.ORGANIZER, calendarEvent.organizer)
            putExtra(CalendarContract.Events.EVENT_LOCATION, calendarEvent.location)
            putExtra(CalendarContract.Events.STATUS, calendarEvent.status)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime.time.time)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.time.time)
        }
    })
}