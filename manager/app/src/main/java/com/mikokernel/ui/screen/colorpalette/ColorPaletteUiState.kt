package com.mikokernel.ui.screen.colorpalette

import androidx.compose.runtime.Immutable
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.mikokernel.ui.screen.settings.SettingsUiState
import com.mikokernel.ui.theme.ColorMode
import com.mikokernel.ui.theme.FontMode

@Immutable
data class ColorPaletteUiState(
    val uiState: SettingsUiState,
    val currentColorMode: ColorMode,
    val currentPaletteStyle: PaletteStyle,
    val currentColorSpec: ColorSpec.SpecVersion,
    val currentFontMode: FontMode = FontMode.DEFAULT,
)

@Immutable
data class ColorPaletteScreenActions(
    val onBack: () -> Unit,
    val onSetThemeMode: (Int) -> Unit,
    val onSetKeyColor: (Int) -> Unit,
    val onSetColorMode: (ColorMode) -> Unit,
    val onSetColorStyle: (String) -> Unit,
    val onSetColorSpec: (String) -> Unit,
    val onSetEnablePredictiveBack: (Boolean) -> Unit,
    val onSetPageScale: (Float) -> Unit,
    val onSetFontMode: (FontMode) -> Unit,
)
