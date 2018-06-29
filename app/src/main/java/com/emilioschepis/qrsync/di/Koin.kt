package com.emilioschepis.qrsync.di

import com.emilioschepis.qrsync.repository.*
import com.emilioschepis.qrsync.ui.codelist.CodeListViewModel
import com.emilioschepis.qrsync.ui.detail.DetailViewModel
import com.emilioschepis.qrsync.ui.preferences.PreferencesViewModel
import com.emilioschepis.qrsync.ui.scan.ScanViewModel
import com.emilioschepis.qrsync.ui.signin.SignInViewModel
import com.emilioschepis.qrsync.ui.signup.SignUpViewModel
import com.emilioschepis.qrsync.ui.splash.SplashViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.ml.vision.FirebaseVision
import org.koin.android.architecture.ext.viewModel
import org.koin.dsl.module.applicationContext

private val firebaseModule = applicationContext {
    bean { FirebaseAuth.getInstance() as FirebaseAuth }
    bean { configuredFirestore }
    bean { FirebaseVision.getInstance() as FirebaseVision }
}

private val repositoryModule = applicationContext {
    bean { AuthRepositoryImpl(get()) as IAuthRepository }
    bean { FirestoreRepositoryImpl(get(), get()) as IFirestoreRepository }
    bean { VisionRepositoryImpl(get()) as IVisionRepository }
}

private val viewModelModule = applicationContext {
    viewModel { SplashViewModel(get()) }
    viewModel { SignInViewModel(get()) }
    viewModel { SignUpViewModel(get()) }
    viewModel { params -> DetailViewModel(params["id"], get()) }
    viewModel { ScanViewModel(get(), get()) }
    viewModel { CodeListViewModel(get()) }
    viewModel { PreferencesViewModel(get(), get()) }
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

val koinModules = listOf(
        firebaseModule,
        repositoryModule,
        viewModelModule)