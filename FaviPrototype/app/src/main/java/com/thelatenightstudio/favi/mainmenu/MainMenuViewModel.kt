package com.thelatenightstudio.favi.mainmenu

import androidx.lifecycle.ViewModel
import com.thelatenightstudio.favi.core.domain.usecase.FaviUseCase

class MainMenuViewModel(private val faviUseCase: FaviUseCase) : ViewModel() {

    fun signOut() {
        faviUseCase.signOut()
    }

    fun activateBiometric(){
        faviUseCase.activateBiometric()
    }

}