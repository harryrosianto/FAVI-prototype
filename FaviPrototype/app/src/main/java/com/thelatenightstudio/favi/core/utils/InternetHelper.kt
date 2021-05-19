package com.thelatenightstudio.favi.core.utils

import android.util.Log

object InternetHelper {

    fun isConnected(): Boolean {
        var isConnected = false
        try {
            val command = "ping -c 1 google.com"
            isConnected = Runtime.getRuntime().exec(command).waitFor() == 0
        } catch (e: Exception) {
            Log.d("InternetHelper", "isConnected: ${e.message}")
        }
        return isConnected
    }

}