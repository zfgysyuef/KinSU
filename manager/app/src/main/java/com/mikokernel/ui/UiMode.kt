package com.mikokernel.ui

import androidx.compose.runtime.staticCompositionLocalOf

enum class UiMode(val value: String) {
    Material("material"),
    MaterialExpressive("material_expressive");

    companion object {
        fun fromValue(value: String): UiMode = when (value) {
            MaterialExpressive.value -> MaterialExpressive
            else -> Material
        }
        val DEFAULT_VALUE = Material.value
    }
}

val LocalUiMode = staticCompositionLocalOf { UiMode.Material }
