package com.thelatenightstudio.favi.core.service

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.thelatenightstudio.favi.core.domain.usecase.FaviUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SignOutService : JobIntentService() {

    companion object {
        private const val JOB_ID = 100
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, SignOutService::class.java, JOB_ID, intent)
        }
    }

    private val faviUseCase: FaviUseCase by inject()

    override fun onHandleWork(intent: Intent) {
        CoroutineScope(IO).launch {
            faviUseCase.signOut()
        }
    }
}