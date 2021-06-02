package com.thelatenightstudio.favi.core.domain.usecase

import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import com.thelatenightstudio.favi.core.domain.model.User
import com.thelatenightstudio.favi.core.domain.repository.IFaviRepository
import kotlinx.coroutines.flow.Flow

class FaviInteractor(private val faviRepository: IFaviRepository) : FaviUseCase {

    override suspend fun createUser(email: String, password: String): Flow<ApiResponse<Boolean>> =
        faviRepository.createUser(email, password)

    override suspend fun signIn(email: String, password: String): Flow<ApiResponse<Boolean>> {
        faviRepository.saveTemporaryCredentialsToSharedPref(email, password)
        return faviRepository.signIn(email, password)
    }

    override suspend fun signOut() {
        faviRepository.deleteTemporaryCredentialsFromSharedPref()
        faviRepository.signOut()
    }

    override suspend fun getIdToken(): Flow<ApiResponse<String>> =
        faviRepository.getIdToken()

    override suspend fun signInWithCustomToken(token: String): Flow<ApiResponse<Boolean>> =
        faviRepository.signInWithCustomToken(token)

    override suspend fun signInWithBiometric(): Flow<ApiResponse<Boolean>> {
        val email = faviRepository.getEmailFromSharedPref()
        val password = faviRepository.getPasswordFromSharedPref()
        return faviRepository.signIn(email, password)
    }

    override suspend fun isBiometricActive(): Boolean =
        faviRepository.getBiometricAuthFromSharedPref()

    override suspend fun activateBiometric(): Boolean =
        faviRepository.activateBiometric()

    override suspend fun getDataOfCurrentUser(): Flow<ApiResponse<User>> =
        faviRepository.getDataOfCurrentUser()

    override suspend fun increaseBalanceOfCurrentUser(requestAmount: Double): Flow<ApiResponse<Boolean>> =
        faviRepository.increaseBalanceOfCurrentUser(requestAmount)

    override suspend fun getRealtimeUpdatesOfCurrentUser(): Flow<ApiResponse<User>> =
        faviRepository.getRealtimeUpdatesOfCurrentUser()

    override suspend fun transferBalanceToAnotherUser(
        targetEmail: String,
        requestAmount: Double
    ): Flow<ApiResponse<Boolean>> =
        faviRepository.transferBalanceToAnotherUser(targetEmail, requestAmount)

    override suspend fun uploadFile(
        filePath: String
    ): Flow<ApiResponse<Boolean>> =
        faviRepository.uploadFile(filePath)

}