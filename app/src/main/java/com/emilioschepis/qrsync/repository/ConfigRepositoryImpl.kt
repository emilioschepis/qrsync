package com.emilioschepis.qrsync.repository

import android.net.Uri
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

class ConfigRepositoryImpl(private val config: FirebaseRemoteConfig) : IConfigRepository {
    override val instagramHandle: Uri
        get() {
            val handle = config.getString("instagram_handle")
            return Uri.parse("https://www.instagram.com/$handle/")
        }
    override val twitterHandle: Uri
        get() {
            val handle = config.getString("twitter_handle")
            return Uri.parse("https://twitter.com/$handle/")
        }
    override val linkedInHandle: Uri
        get() {
            val handle = config.getString("linkedin_handle")
            return Uri.parse("https://www.linkedin.com/in/$handle/")
        }
    override val gitHubHandle: Uri
        get() {
            val handle = config.getString("github_handle")
            return Uri.parse("https://github.com/$handle/")
        }
    override val playStoreHandle: Uri
        get() {
            val handle = config.getString("playstore_handle")
            return Uri.parse("http://play.google.com/store/apps/dev?id=$handle")
        }
    override val generalInfo: String
        get() {
            return config.getString("general_info")
                    .replace("\\n", "\n")
        }
    override val websiteUrl: Uri
        get() {
            val remoteString = config.getString("website_url")
            return Uri.parse(remoteString)
        }
}