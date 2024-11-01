package com.github.nullptroma.wallenc.presentation.screens.main

import androidx.lifecycle.ViewModel
import com.github.nullptroma.wallenc.domain.usecases.TestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class MainViewModel @javax.inject.Inject constructor(
    testUseCase: TestUseCase,
    testUseCase2: TestUseCase,
    testUseCase3: TestUseCase,
): ViewModel() {
    val stateFlow = MainScreenState("${testUseCase3.meta.name}, number ${testUseCase3.id}")
}