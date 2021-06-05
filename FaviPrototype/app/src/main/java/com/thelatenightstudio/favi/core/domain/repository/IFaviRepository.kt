package com.thelatenightstudio.favi.core.domain.repository

import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import com.thelatenightstudio.favi.core.domain.model.User
import kotlinx.coroutines.flow.Flow

interface IFaviRepository {

    suspend fun createUser(email: String, password: String): Flow<ApiResponse<Boolean>>

    suspend fun signIn(email: String, password: String): Flow<ApiResponse<Boolean>>

    suspend fun signOut()

    suspend fun getIdToken(): Flow<ApiResponse<String>>

    suspend fun signInWithCustomToken(token: String): Flow<ApiResponse<Boolean>>

    suspend fun getEmailFromSharedPref(): String

    suspend fun getPasswordFromSharedPref(): String

    suspend fun getBiometricAuthFromSharedPref(): Boolean

    suspend fun saveTemporaryCredentialsToSharedPref(email: String, password: String)

    suspend fun deleteTemporaryCredentialsFromSharedPref()

    suspend fun activateBiometric(): Boolean

    suspend fun getDataOfCurrentUser(): Flow<ApiResponse<User>>

    suspend fun increaseBalanceOfCurrentUser(requestAmount: Double): Flow<ApiResponse<Boolean>>

    suspend fun getRealtimeUpdatesOfCurrentUser(): Flow<ApiResponse<User>>

    suspend fun transferBalanceToAnotherUser(
        targetEmail: String,
        requestAmount: Double
    ): Flow<ApiResponse<Boolean>>

    suspend fun uploadFile(filePath: String): Flow<ApiResponse<Boolean>>

    suspend fun resetPredictionFieldOfCurrentUser()

}