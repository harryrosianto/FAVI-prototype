package com.thelatenightstudio.favi.core.domain.usecase

import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import kotlinx.coroutines.flow.Flow

interface FaviUseCase {

    fun createUser(email: String, password: String): Flow<ApiResponse<Boolean>>

    fun signIn(email: String, password: String): Flow<ApiResponse<Boolean>>

    fun signOut()

    fun getIdToken(): Flow<ApiResponse<String>>

    fun signInWithCustomToken(token: String): Flow<ApiResponse<Boolean>>

    fun signInWithBiometric(): Flow<ApiResponse<Boolean>>

    fun isBiometricActive(): Boolean

    fun activateBiometric()

}