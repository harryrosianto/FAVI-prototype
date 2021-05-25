package com.thelatenightstudio.favi.mainmenu

import androidx.lifecycle.ViewModel
import com.thelatenightstudio.favi.core.domain.usecase.FaviUseCase

class MainMenuViewModel(private val faviUseCase: FaviUseCase) : ViewModel() {

    suspend fun signOut() {
        faviUseCase.signOut()
    }

    suspend fun activateBiometric(): Boolean =
        faviUseCase.activateBiometric()

}