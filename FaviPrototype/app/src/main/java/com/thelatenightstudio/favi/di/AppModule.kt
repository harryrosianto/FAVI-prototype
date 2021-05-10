package com.thelatenightstudio.favi.di

import android.os.Build
import androidx.annotation.RequiresApi
import com.thelatenightstudio.favi.security.CryptographyManager
import com.thelatenightstudio.favi.security.CryptographyManagerImpl
import org.koin.dsl.module

@RequiresApi(Build.VERSION_CODES.M)
@JvmField
val appModule = module {
    factory<CryptographyManager> { CryptographyManagerImpl() }
}