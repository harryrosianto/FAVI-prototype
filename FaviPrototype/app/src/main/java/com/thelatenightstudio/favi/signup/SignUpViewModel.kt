package com.thelatenightstudio.favi.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import com.thelatenightstudio.favi.core.domain.usecase.FaviUseCase

class SignUpViewModel(private val faviUseCase: FaviUseCase) : ViewModel() {

    fun createUser(email: String, password: String): LiveData<ApiResponse<Boolean>> =
        faviUseCase.createUser(email, password).asLiveData()

}