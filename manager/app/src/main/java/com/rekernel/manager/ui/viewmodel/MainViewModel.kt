package com.rekernel.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.rekernel.manager.ui.theme.AppSettings
import com.rekernel.manager.ui.theme.ColorMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AppSettings())
    val uiState: StateFlow<AppSettings> = _uiState

    private val _selectedPage = MutableStateFlow(0)
    val selectedPage: StateFlow<Int> = _selectedPage

    fun setSelectedPage(page: Int) {
        _selectedPage.value = page
    }

    fun setColorMode(mode: ColorMode) {
        _uiState.value = _uiState.value.copy(colorMode = mode)
    }

    fun setUiMode(uiMode: String) {
        _uiState.value = _uiState.value.copy(uiMode = uiMode)
    }
}
