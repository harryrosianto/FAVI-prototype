package com.thelatenightstudio.favi.core.utils

import android.widget.EditText

object EditTextHelper {

    fun showEditTextExistAlert(editText: EditText, isNotValid: Boolean, errorText: String) {
        editText.error = if (isNotValid) errorText else null
    }

}