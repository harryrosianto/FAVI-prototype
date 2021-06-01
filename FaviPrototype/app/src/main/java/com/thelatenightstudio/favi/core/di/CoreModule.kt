package com.thelatenightstudio.favi.core.di

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.thelatenightstudio.favi.core.data.FaviRepository
import com.thelatenightstudio.favi.core.data.source.local.SharedPreferencesManager
import com.thelatenightstudio.favi.core.data.source.remote.RemoteDataSource
import com.thelatenightstudio.favi.core.domain.repository.IFaviRepository
import com.thelatenightstudio.favi.core.media.Model
import com.thelatenightstudio.favi.core.media.ModelWithAudioRecord
import com.thelatenightstudio.favi.core.media.Recorder
import org.koin.dsl.module

val networkModule = module {
    single {
        FirebaseAuth.getInstance()
    }
    single {
        FirebaseFirestore.getInstance()
    }

}

val repositoryModule = module {
    single { RemoteDataSource(get(), get()) }
    single { SharedPreferencesManager(get()) }
    single<IFaviRepository> {
        FaviRepository(get(), get())
    }
}

@RequiresApi(Build.VERSION_CODES.M)
val mediaModule = module {
    single { Recorder(get()) }
    single { ModelWithAudioRecord(get()) }
    single { Model(get()) }
}