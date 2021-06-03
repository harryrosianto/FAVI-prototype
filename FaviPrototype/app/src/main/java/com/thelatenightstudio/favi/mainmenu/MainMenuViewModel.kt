package com.thelatenightstudio.favi.mainmenu

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import com.thelatenightstudio.favi.core.domain.model.User
import com.thelatenightstudio.favi.core.domain.usecase.FaviUseCase

class MainMenuViewModel(private val faviUseCase: FaviUseCase) : ViewModel() {

    suspend fun activateBiometric(): Boolean =
        faviUseCase.activateBiometric()

    suspend fun getDataOfCurrentUser(): LiveData<ApiResponse<User>> =
        faviUseCase.getDataOfCurrentUser().asLiveData()

    suspend fun getRealtimeUpdatesOfCurrentUser(): LiveData<ApiResponse<User>> =
        faviUseCase.getRealtimeUpdatesOfCurrentUser().asLiveData()

    suspend fun uploadFile(filePath: String): LiveData<ApiResponse<Boolean>> =
        faviUseCase.uploadFile(filePath).asLiveData()

}