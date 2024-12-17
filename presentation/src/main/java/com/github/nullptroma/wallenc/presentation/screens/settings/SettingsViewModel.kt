package com.github.nullptroma.wallenc.presentation.screens.settings

import com.github.nullptroma.wallenc.presentation.viewmodel.ViewModelBase
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class SettingsViewModel @javax.inject.Inject constructor() :
    ViewModelBase<SettingsScreenState>(SettingsScreenState()) {
}