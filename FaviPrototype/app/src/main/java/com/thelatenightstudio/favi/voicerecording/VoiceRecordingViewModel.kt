package com.thelatenightstudio.favi.voicerecording

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.thelatenightstudio.favi.core.data.source.remote.network.ApiResponse
import com.thelatenightstudio.favi.core.domain.usecase.FaviUseCase

class VoiceRecordingViewModel(private val faviUseCase: FaviUseCase) : ViewModel() {

    suspend fun uploadFile(filePath: String): LiveData<ApiResponse<Boolean>> =
        faviUseCase.uploadFile(filePath).asLiveData()

}