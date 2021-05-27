package com.thelatenightstudio.favi.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import com.thelatenightstudio.favi.core.domain.usecase.FaviUseCase

class SignUpViewModel(private val faviUseCase: FaviUseCase) : ViewModel() {

    private lateinit var email: String
    private lateinit var password: String

    fun setEmail(email: String) {
        this.email = email
    }

    fun setPassword(password: String) {
        this.password = password
    }

    suspend fun createUser(): LiveData<ApiResponse<Boolean>> =
        faviUseCase.createUser(email, password).asLiveData()

}