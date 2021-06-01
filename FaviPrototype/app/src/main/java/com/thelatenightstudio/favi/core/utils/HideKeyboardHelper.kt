package com.thelatenightstudio.favi.core.utils

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

object HideKeyboardHelper {

    fun View.hideKeyboard() = ViewCompat.getWindowInsetsController(this)
        ?.hide(WindowInsetsCompat.Type.ime())

    fun Fragment.hideKeyboard() = ViewCompat.getWindowInsetsController(requireView())
        ?.hide(WindowInsetsCompat.Type.ime())

}