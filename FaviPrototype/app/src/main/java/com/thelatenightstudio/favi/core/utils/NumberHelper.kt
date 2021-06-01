package com.thelatenightstudio.favi.core.utils

import android.icu.text.NumberFormat
import android.os.Build
import androidx.annotation.RequiresApi
import java.util.concurrent.TimeUnit

object NumberHelper {

    fun Long.formatAsTime(): String {
        val seconds = (TimeUnit.MILLISECONDS.toSeconds(this) % 60).toInt()
        val minutes = (TimeUnit.MILLISECONDS.toMinutes(this) % 60).toInt()

        return when (val hours = (TimeUnit.MILLISECONDS.toHours(this)).toInt()) {
            0 -> String.format("%02d:%02d", minutes, seconds)
            else -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun Double.formatAsBalance(): String {
        return when (this % 1) {
            .0 -> NumberFormat.getNumberInstance().format(this.toInt())
            else -> NumberFormat.getNumberInstance().format(this)
        }
    }

}