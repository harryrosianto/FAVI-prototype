package com.thelatenightstudio.favi.core.security

data class EncryptedData(
    val ciphertext: ByteArray,
    val initializationVector: ByteArray
)