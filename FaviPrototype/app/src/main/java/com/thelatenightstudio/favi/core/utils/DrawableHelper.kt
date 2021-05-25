package com.thelatenightstudio.favi.core.utils

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

object DrawableHelper {

    fun Context.getDrawableCompat(@DrawableRes resId: Int) =
        ContextCompat.getDrawable(this, resId)

}