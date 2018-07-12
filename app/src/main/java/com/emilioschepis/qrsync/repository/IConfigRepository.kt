package com.emilioschepis.qrsync.repository

import android.net.Uri

interface IConfigRepository {
    val instagramHandle: Uri
    val twitterHandle: Uri
    val gitHubHandle: Uri
    val linkedInHandle: Uri
    val playStoreHandle: Uri
    val generalInfo: String
}