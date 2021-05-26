package com.thelatenightstudio.favi.core.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.invoke

object InternetHelper {

    suspend fun isConnected(): Boolean =
        (Default){
            var isConnected = false
            try {
                val command = "ping -c 1 google.com"
                isConnected = Runtime.getRuntime().exec(command).waitFor() == 0
            } catch (e: Exception) {
                Log.d("InternetHelper", "isConnected: ${e.message}")
            }
            isConnected
        }

}