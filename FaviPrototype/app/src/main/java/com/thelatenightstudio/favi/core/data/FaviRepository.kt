package com.thelatenightstudio.favi.core.data

import com.thelatenightstudio.favi.core.data.source.remote.RemoteDataSource
import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import com.thelatenightstudio.favi.core.domain.repository.IFaviRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

class FaviRepository(private val remoteDataSource: RemoteDataSource) : IFaviRepository {

    @ExperimentalCoroutinesApi
    override fun createUser(email: String, password: String): Flow<ApiResponse<Boolean>> =
        remoteDataSource.createUser(email, password)

    @ExperimentalCoroutinesApi
    override fun signIn(email: String, password: String): Flow<ApiResponse<Boolean>> =
        remoteDataSource.signIn(email, password)

    override fun signOut() {
        remoteDataSource.signOut()
    }

    @ExperimentalCoroutinesApi
    override fun getIdToken(): Flow<ApiResponse<String>> =
        remoteDataSource.getIdToken()

    @ExperimentalCoroutinesApi
    override fun signInWithCustomToken(token: String): Flow<ApiResponse<Boolean>> =
        remoteDataSource.signInWithCustomToken(token)

}