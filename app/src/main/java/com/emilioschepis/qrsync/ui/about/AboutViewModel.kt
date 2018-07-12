package com.emilioschepis.qrsync.ui.about

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.emilioschepis.qrsync.model.QSHandle
import com.emilioschepis.qrsync.repository.IConfigRepository

class AboutViewModel(private val config: IConfigRepository) : ViewModel() {
    val generalInfo: LiveData<String>
        get() {
            return MutableLiveData<String>()
                    .apply { value = config.generalInfo }
        }

    val handles: LiveData<List<QSHandle>>
        get() {
            return MutableLiveData<List<QSHandle>>()
                    .apply { value = listOf(QSHandle.Instagram, QSHandle.Twitter, QSHandle.LinkedIn, QSHandle.GitHub, QSHandle.PlayStore) }
        }

    val instagramHandle = config.instagramHandle
    val twitterHandle = config.twitterHandle
    val githubHandle = config.gitHubHandle
    val linkedInHandle = config.linkedInHandle
    val playStoreHandle = config.playStoreHandle
}
