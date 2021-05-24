package com.thelatenightstudio.favi.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import com.thelatenightstudio.favi.core.domain.usecase.FaviUseCase

class MainMenuViewModel(private val faviUseCase: FaviUseCase) : ViewModel() {

    fun signOut() {
        faviUseCase.signOut()
    }

    fun getIdToken(): LiveData<ApiResponse<String>> =
        faviUseCase.getIdToken().asLiveData()

}