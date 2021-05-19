package com.thelatenightstudio.favi.core.data

import com.thelatenightstudio.favi.core.data.source.remote.RemoteDataSource
import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import com.thelatenightstudio.favi.core.domain.repository.IFaviRepository
import kotlinx.coroutines.flow.Flow

class FaviRepository(private val remoteDataSource: RemoteDataSource) : IFaviRepository {

    override fun createUser(email: String, password: String): Flow<ApiResponse<Boolean>> =
        remoteDataSource.createUser(email, password)

}