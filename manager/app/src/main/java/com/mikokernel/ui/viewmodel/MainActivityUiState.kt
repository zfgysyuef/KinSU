package com.mikokernel.ui.viewmodel

import androidx.compose.runtime.Immutable
import com.mikokernel.ui.UiMode
import com.mikokernel.ui.theme.AppSettings

@Immutable
data class MainActivityUiState(
    val appSettings: AppSettings,
    val pageScale: Float,
    val uiMode: UiMode,
)
