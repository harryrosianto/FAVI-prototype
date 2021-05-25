package com.thelatenightstudio.favi.core.domain.repository

import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import kotlinx.coroutines.flow.Flow

interface IFaviRepository {

    fun createUser(email: String, password: String): Flow<ApiResponse<Boolean>>

    fun signIn(email: String, password: String): Flow<ApiResponse<Boolean>>

    fun signOut()

    fun getIdToken(): Flow<ApiResponse<String>>

    fun signInWithCustomToken(token: String): Flow<ApiResponse<Boolean>>

    fun getEmailFromSharedPref(): String

    fun getPasswordFromSharedPref(): String

    fun getBiometricAuthFromSharedPref(): Boolean

    fun saveTemporaryCredentialsToSharedPref(email: String, password: String)

    fun deleteTemporaryCredentialsFromSharedPref()

    fun activateBiometric()

}