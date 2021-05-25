package com.thelatenightstudio.favi.signin

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import com.thelatenightstudio.favi.core.domain.usecase.FaviUseCase

class SignInViewModel(private val faviUseCase: FaviUseCase) : ViewModel() {

    suspend fun signIn(email: String, password: String): LiveData<ApiResponse<Boolean>> =
        faviUseCase.signIn(email, password).asLiveData()

    suspend fun signInWithBiometric(): LiveData<ApiResponse<Boolean>> =
        faviUseCase.signInWithBiometric().asLiveData()

    suspend fun isBiometricActive(): Boolean =
        faviUseCase.isBiometricActive()

}