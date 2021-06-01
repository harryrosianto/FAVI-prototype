package com.thelatenightstudio.favi.transfermenu

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import com.thelatenightstudio.favi.core.domain.usecase.FaviUseCase

class TransferMenuViewModel(private val faviUseCase: FaviUseCase) : ViewModel() {

    suspend fun transferBalanceToAnotherUser(
        targetEmail: String,
        requestAmount: Double
    ): LiveData<ApiResponse<Boolean>> =
        faviUseCase.transferBalanceToAnotherUser(targetEmail, requestAmount).asLiveData()

}