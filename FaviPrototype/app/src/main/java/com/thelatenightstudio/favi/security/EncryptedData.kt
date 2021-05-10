package com.thelatenightstudio.favi.security

data class EncryptedData(
    val ciphertext: ByteArray,
    val initializationVector: ByteArray
)