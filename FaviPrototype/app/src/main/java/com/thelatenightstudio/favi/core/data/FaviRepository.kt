package com.thelatenightstudio.favi.core.data

import com.thelatenightstudio.favi.core.data.source.local.SharedPreferencesManager
import com.thelatenightstudio.favi.core.data.source.remote.RemoteDataSource
import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import com.thelatenightstudio.favi.core.domain.model.User
import com.thelatenightstudio.favi.core.domain.repository.IFaviRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

class FaviRepository(
    private val remoteDataSource: RemoteDataSource,
    private val sharedPreferencesManager: SharedPreferencesManager
) : IFaviRepository {

    @ExperimentalCoroutinesApi
    override suspend fun createUser(email: String, password: String): Flow<ApiResponse<Boolean>> =
        remoteDataSource.createUser(email, password)

    @ExperimentalCoroutinesApi
    override suspend fun signIn(email: String, password: String): Flow<ApiResponse<Boolean>> =
        remoteDataSource.signIn(email, password)

    override suspend fun signOut() {
        remoteDataSource.signOut()
    }

    @ExperimentalCoroutinesApi
    override suspend fun getIdToken(): Flow<ApiResponse<String>> =
        remoteDataSource.getIdToken()

    @ExperimentalCoroutinesApi
    override suspend fun signInWithCustomToken(token: String): Flow<ApiResponse<Boolean>> =
        remoteDataSource.signInWithCustomToken(token)

    override suspend fun getEmailFromSharedPref(): String =
        sharedPreferencesManager.getString(SharedPreferencesManager.USERNAME)

    override suspend fun getPasswordFromSharedPref(): String =
        sharedPreferencesManager.getString(SharedPreferencesManager.PASSWORD)

    override suspend fun getBiometricAuthFromSharedPref(): Boolean =
        sharedPreferencesManager.getBoolean(SharedPreferencesManager.BIOMETRIC_AUTH)

    override suspend fun saveTemporaryCredentialsToSharedPref(email: String, password: String) {
        sharedPreferencesManager.signIn(email, password)
    }

    override suspend fun deleteTemporaryCredentialsFromSharedPref() {
        sharedPreferencesManager.signOut()
    }

    override suspend fun activateBiometric(): Boolean =
        sharedPreferencesManager.activateBiometricAuth()

    @ExperimentalCoroutinesApi
    override suspend fun getDataOfCurrentUser(): Flow<ApiResponse<User>> =
        remoteDataSource.getDataOfCurrentUser()

}