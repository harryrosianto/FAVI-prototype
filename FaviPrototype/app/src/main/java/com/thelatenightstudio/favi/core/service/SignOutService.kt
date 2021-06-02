package com.thelatenightstudio.favi.core.service

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.android.inject

class SignOutService : JobIntentService() {

    companion object {
        private const val JOB_ID = 100
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, SignOutService::class.java, JOB_ID, intent)
        }
    }

    private val firebaseAuth: FirebaseAuth by inject()

    override fun onHandleWork(intent: Intent) {
        firebaseAuth.signOut()
    }
}