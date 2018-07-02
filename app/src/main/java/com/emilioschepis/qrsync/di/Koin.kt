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
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

private val firebaseModule = module {
    single { FirebaseAuth.getInstance() as FirebaseAuth }
    single { configuredFirestore }
    single { FirebaseVision.getInstance() as FirebaseVision }
}

private val repositoryModule = module {
    single { AuthRepositoryImpl(get()) as IAuthRepository }
    single { FirestoreRepositoryImpl(get(), get()) as IFirestoreRepository }
    single { VisionRepositoryImpl(get()) as IVisionRepository }
}

private val viewModelModule = module {
    viewModel { SplashViewModel(get()) }
    viewModel { SignInViewModel(get()) }
    viewModel { SignUpViewModel(get()) }
    viewModel { (id: String) -> DetailViewModel(id, get()) }
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