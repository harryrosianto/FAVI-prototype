package com.thelatenightstudio.favi.core.domain.usecase

import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import kotlinx.coroutines.flow.Flow

interface FaviUseCase {

    fun createUser(email: String, password: String): Flow<ApiResponse<Boolean>>

    fun signIn(email: String, password: String): Flow<ApiResponse<Boolean>>

}