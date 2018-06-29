package com.emilioschepis.qrsync

import android.app.Application
import com.emilioschepis.qrsync.di.koinModules
import org.koin.android.ext.android.startKoin

class QSApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin(this, koinModules)
    }
}