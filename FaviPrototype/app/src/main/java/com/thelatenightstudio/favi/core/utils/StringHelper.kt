package com.thelatenightstudio.favi.core.utils

object StringHelper {

    const val NOT_A_NUMBER = "not_a_number"

    fun String.toNumber(): String {
        return when (this) {
            "nol" -> "0"
            "satu" -> "1"
            "dua" -> "2"
            "tiga" -> "3"
            "empat" -> "4"
            "lima" -> "5"
            "enam" -> "6"
            "tujuh" -> "7"
            "delapan" -> "8"
            "sembilan" -> "9"
            else -> NOT_A_NUMBER
        }
    }

}