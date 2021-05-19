package com.thelatenightstudio.favi.core.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class AppCoroutineScopes(
    private val main: CoroutineScope,
    private val io: CoroutineScope,
    private val default: CoroutineScope
) {

    constructor() : this(
        CoroutineScope(Dispatchers.Main),
        CoroutineScope(Dispatchers.IO),
        CoroutineScope(Dispatchers.Default)
    )

    fun main(): CoroutineScope = main
    fun io(): CoroutineScope = io
    fun default(): CoroutineScope = default

}