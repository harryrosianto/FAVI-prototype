package com.thelatenightstudio.favi.core.utils

import android.content.Context
import android.widget.Toast

object ToastHelper {

    fun Context.showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}