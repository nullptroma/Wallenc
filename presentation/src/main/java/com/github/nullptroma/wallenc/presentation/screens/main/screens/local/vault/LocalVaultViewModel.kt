package com.github.nullptroma.wallenc.presentation.screens.main.screens.local.vault

import com.github.nullptroma.wallenc.presentation.viewmodel.ViewModelBase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocalVaultViewModel @Inject constructor()
    : ViewModelBase<LocalVaultScreenState>(LocalVaultScreenState("default")) {

}