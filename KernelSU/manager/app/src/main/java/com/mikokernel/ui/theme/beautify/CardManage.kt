package com.mikokernel.ui.theme.beautify

import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * Card elevation helper - like FolkPatch's CardManage.
 * Cards become flat (0.dp elevation) when wallpaper is active,
 * so the wallpaper shows through without shadow interference.
 */
@Composable
fun cardElevation(): CardElevation = CardDefaults.cardElevation(
    defaultElevation = if (BackgroundConfig.hasAnyWallpaper) 0.dp else 6.dp
)