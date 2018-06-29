package com.emilioschepis.qrsync.ui.splash

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import arrow.core.Option
import com.emilioschepis.qrsync.model.QSUser
import com.emilioschepis.qrsync.repository.IAuthRepository

class SplashViewModel(private val auth: IAuthRepository) : ViewModel() {
    private val mutableAuthentication = MutableLiveData<Option<QSUser>>()

    val authentication: LiveData<Option<QSUser>>
        get() {
            mutableAuthentication.value = auth.getCurrentUser()
            return mutableAuthentication
        }
}