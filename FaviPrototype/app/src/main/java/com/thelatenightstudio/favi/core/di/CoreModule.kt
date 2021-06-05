package com.thelatenightstudio.favi.core.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.thelatenightstudio.favi.core.data.FaviRepository
import com.thelatenightstudio.favi.core.data.source.local.SharedPreferencesManager
import com.thelatenightstudio.favi.core.data.source.remote.RemoteDataSource
import com.thelatenightstudio.favi.core.domain.repository.IFaviRepository
import com.thelatenightstudio.favi.core.media.Recorder
import org.koin.dsl.module

val networkModule = module {
    single {
        FirebaseAuth.getInstance()
    }
    single {
        FirebaseFirestore.getInstance()
    }
    single {
        FirebaseStorage.getInstance("gs://user_voice_input").reference
    }
}

val repositoryModule = module {
    single { RemoteDataSource(get(), get(), get()) }
    single { SharedPreferencesManager(get()) }
    single<IFaviRepository> {
        FaviRepository(get(), get())
    }
}

val mediaModule = module {
    factory { Recorder(get()) }
}