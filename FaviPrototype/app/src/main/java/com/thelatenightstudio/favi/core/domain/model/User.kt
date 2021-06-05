package com.thelatenightstudio.favi.core.domain.model

data class User(
    var uid: String = "",
    val balance: Double = .0,
    var email: String = "",
    var prediction: String = ""
)