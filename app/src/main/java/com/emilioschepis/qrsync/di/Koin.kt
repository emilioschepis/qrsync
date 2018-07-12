package com.emilioschepis.qrsync.di

import com.emilioschepis.qrsync.R
import com.emilioschepis.qrsync.repository.*
import com.emilioschepis.qrsync.ui.about.AboutViewModel
import com.emilioschepis.qrsync.ui.codelist.CodeListViewModel
import com.emilioschepis.qrsync.ui.detail.DetailViewModel
import com.emilioschepis.qrsync.ui.preferences.PreferencesViewModel
import com.emilioschepis.qrsync.ui.scan.ScanViewModel
import com.emilioschepis.qrsync.ui.signin.SignInViewModel
import com.emilioschepis.qrsync.ui.signup.SignUpViewModel
import com.emilioschepis.qrsync.ui.splash.SplashViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.BuildConfig
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

private val firebaseModule = module {
    single { FirebaseAuth.getInstance() as FirebaseAuth }
    single { configuredFirestore }
    single { configuredRemoteConfig }
    single { FirebaseVision.getInstance() as FirebaseVision }
}

private val repositoryModule = module {
    single { AuthRepositoryImpl(get()) as IAuthRepository }
    single { FirestoreRepositoryImpl(get(), get()) as IFirestoreRepository }
    single { VisionRepositoryImpl(get()) as IVisionRepository }
    single { ConfigRepositoryImpl(get()) as IConfigRepository }
}

private val viewModelModule = module {
    viewModel { SplashViewModel(get()) }
    viewModel { SignInViewModel(get()) }
    viewModel { SignUpViewModel(get()) }
    viewModel { (id: String) -> DetailViewModel(id, get()) }
    viewModel { ScanViewModel(get(), get()) }
    viewModel { CodeListViewModel(get()) }
    viewModel { PreferencesViewModel(get(), get()) }
    viewModel { AboutViewModel(get()) }
}

private val configuredFirestore: FirebaseFirestore
    get() {
        val settings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .setPersistenceEnabled(true)
                .build()

        val firestore = FirebaseFirestore.getInstance()

        firestore.firestoreSettings = settings

        return firestore
    }

private val configuredRemoteConfig: FirebaseRemoteConfig
    get() {
        val rc = FirebaseRemoteConfig.getInstance()
        rc.setDefaults(R.xml.remote_config_defaults)

        val settings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()
        rc.setConfigSettings(settings)

        val cacheExpiration = if (rc.info.configSettings.isDeveloperModeEnabled) 0 else 3600

        rc.fetch(cacheExpiration.toLong())
                .addOnSuccessListener { rc.activateFetched() }
                .addOnFailureListener { /* Can't handle this*/ }

        return rc
    }

val koinModules = listOf(
        firebaseModule,
        repositoryModule,
        viewModelModule)