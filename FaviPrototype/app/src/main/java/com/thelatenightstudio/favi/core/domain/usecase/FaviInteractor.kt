package com.thelatenightstudio.favi.core.domain.usecase

import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import com.thelatenightstudio.favi.core.domain.repository.IFaviRepository
import kotlinx.coroutines.flow.Flow

class FaviInteractor(private val faviRepository: IFaviRepository) : FaviUseCase {

    override fun createUser(email: String, password: String): Flow<ApiResponse<Boolean>> =
        faviRepository.createUser(email, password)

    override fun signIn(email: String, password: String): Flow<ApiResponse<Boolean>> =
        faviRepository.signIn(email, password)

    override fun signOut() {
        faviRepository.signOut()
    }

    override fun getIdToken(): Flow<ApiResponse<String>> =
        faviRepository.getIdToken()

    override fun signInWithCustomToken(token: String): Flow<ApiResponse<Boolean>> =
        faviRepository.signInWithCustomToken(token)

}