package com.thelatenightstudio.favi.core.utils

import android.content.Context
import android.widget.Toast

object ToastHelper {

    fun showToast(context: Context, message: String) {
        Toast.makeText(
            context,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

}