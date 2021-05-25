package com.thelatenightstudio.favi.core.di

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.thelatenightstudio.favi.core.data.FaviRepository
import com.thelatenightstudio.favi.core.data.source.local.SharedPreferencesManager
import com.thelatenightstudio.favi.core.data.source.remote.RemoteDataSource
import com.thelatenightstudio.favi.core.domain.repository.IFaviRepository
import com.thelatenightstudio.favi.core.media.Recorder
import com.thelatenightstudio.favi.core.security.CryptographyManager
import com.thelatenightstudio.favi.core.security.CryptographyManagerImpl
import org.koin.dsl.module

val networkModule = module {
    single {
        FirebaseAuth.getInstance()
    }

}

val repositoryModule = module {
    single { RemoteDataSource(get()) }
    single<IFaviRepository> {
        FaviRepository(get(), get())
    }
    single { SharedPreferencesManager(get()) }
}

@RequiresApi(Build.VERSION_CODES.M)
val securityModule = module {
    factory<CryptographyManager> { CryptographyManagerImpl() }
}

val mediaModule = module {
    single { Recorder(get()) }
}