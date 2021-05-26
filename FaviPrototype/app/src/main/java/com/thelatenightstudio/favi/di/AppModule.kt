package com.thelatenightstudio.favi.di

import com.thelatenightstudio.favi.core.domain.usecase.FaviInteractor
import com.thelatenightstudio.favi.core.domain.usecase.FaviUseCase
import com.thelatenightstudio.favi.mainmenu.MainMenuViewModel
import com.thelatenightstudio.favi.signin.SignInViewModel
import com.thelatenightstudio.favi.signup.SignUpViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val useCaseModule = module {
    factory<FaviUseCase> { FaviInteractor(get()) }
}

val viewModelModule = module {
    viewModel { SignUpViewModel(get()) }
    viewModel { SignInViewModel(get()) }
    viewModel { MainMenuViewModel(get()) }
}

