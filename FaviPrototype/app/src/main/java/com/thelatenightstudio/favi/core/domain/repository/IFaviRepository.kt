package com.thelatenightstudio.favi.core.domain.repository

import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import kotlinx.coroutines.flow.Flow

interface IFaviRepository {

    fun createUser(email: String, password: String): Flow<ApiResponse<Boolean>>

}