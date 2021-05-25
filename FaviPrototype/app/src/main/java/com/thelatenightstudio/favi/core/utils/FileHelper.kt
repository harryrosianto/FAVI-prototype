package com.thelatenightstudio.favi.core.utils

import android.content.Context
import java.io.File

object FileHelper {

    val Context.recordFile: File
        get() = File(filesDir, "rec.wav")

}