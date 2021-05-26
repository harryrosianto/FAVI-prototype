package com.thelatenightstudio.favi.core.utils

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.invoke

object ToastHelper {

    suspend fun Context.showToast(message: String) {
        (Main) { Toast.makeText(this@showToast, message, Toast.LENGTH_SHORT).show() }
    }

}