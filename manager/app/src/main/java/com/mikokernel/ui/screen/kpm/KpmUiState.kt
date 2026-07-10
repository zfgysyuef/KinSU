package com.mikokernel.ui.screen.kpm

import androidx.compose.runtime.Immutable
import com.mikokernel.data.model.KpmModuleInfo

@Immutable
data class KpmUiState(
    val isRefreshing: Boolean = false,
    val version: String = "",
    val modules: List<KpmModuleInfo> = emptyList(),
    val error: String? = null,
)
