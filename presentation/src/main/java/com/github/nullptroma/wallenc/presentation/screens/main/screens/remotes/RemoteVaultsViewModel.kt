package com.github.nullptroma.wallenc.presentation.screens.main.screens.remotes

import com.github.nullptroma.wallenc.presentation.viewmodel.ViewModelBase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RemoteVaultsViewModel @Inject constructor() :
    ViewModelBase<RemoteVaultsScreenState>(RemoteVaultsScreenState("")) {

}