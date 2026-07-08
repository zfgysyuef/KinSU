package com.mikokernel.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.mikokernel.ui.theme.beautify.BackgroundConfig
import com.mikokernel.ui.theme.beautify.BackgroundLayer
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowInsetsControllerCompat
import com.materialkolor.rememberDynamicColorScheme

/**
 * Theme that applies wallpaper-aware color scheme transparency
 * (FolkPatch-style: modify theme-level colors instead of per-component).
 */
@Composable
fun MaterialKernelSUTheme(
    appSettings: AppSettings,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = appSettings.colorMode.isDark || (appSettings.colorMode.isSystem && systemDarkTheme)
    val amoledMode = appSettings.colorMode.isAmoled
    val dynamicColor = appSettings.keyColor == 0
    val colorStyle = appSettings.paletteStyle
    val colorSpec = appSettings.colorSpec

    val baseColorScheme = if (dynamicColor) {
        val baseScheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        rememberDynamicColorScheme(
            seedColor = Color.Unspecified,
            isDark = darkTheme,
            isAmoled = amoledMode,
            style = colorStyle,
            specVersion = colorSpec,
            primary = baseScheme.primary,
            secondary = baseScheme.secondary,
            tertiary = baseScheme.tertiary,
            neutral = baseScheme.surface,
            neutralVariant = baseScheme.surfaceVariant,
            error = baseScheme.error
        )
    } else {
        rememberDynamicColorScheme(
            seedColor = Color(appSettings.keyColor),
            isDark = darkTheme,
            isAmoled = amoledMode,
            style = colorStyle,
            specVersion = colorSpec,
        )
    }

    // FolkPatch-style: when wallpaper is active, make theme-level surfaces transparent
    val useCustomBackground = BackgroundConfig.hasAnyWallpaper
    val colorScheme = if (useCustomBackground) {
        baseColorScheme.copy(
            background = Color.Transparent,
            surface = baseColorScheme.surface.copy(alpha = BackgroundConfig.cardOpacity),
            surfaceContainer = baseColorScheme.surfaceContainer.copy(alpha = BackgroundConfig.cardOpacity),
            surfaceContainerLow = baseColorScheme.surfaceContainerLow.copy(alpha = BackgroundConfig.cardOpacity),
            surfaceContainerHigh = baseColorScheme.surfaceContainerHigh.copy(alpha = BackgroundConfig.cardOpacity),
            surfaceContainerHighest = baseColorScheme.surfaceContainerHighest.copy(alpha = BackgroundConfig.cardOpacity),
            surfaceVariant = baseColorScheme.surfaceVariant.copy(alpha = BackgroundConfig.cardOpacity),
            secondaryContainer = baseColorScheme.secondaryContainer.copy(alpha = BackgroundConfig.cardOpacity),
            tertiaryContainer = baseColorScheme.tertiaryContainer.copy(alpha = BackgroundConfig.cardOpacity),
            primaryContainer = baseColorScheme.primaryContainer.copy(alpha = BackgroundConfig.cardOpacity),
        )
    } else {
        baseColorScheme
    }

    LaunchedEffect(darkTheme) {
        val window = (context as? Activity)?.window ?: return@LaunchedEffect
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        shapes = KinSUShapes,
        motionScheme = MotionScheme.expressive(),
        content = content,
    )
}

@Composable
fun MaterialExpressiveKernelSUTheme(
    appSettings: AppSettings,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = appSettings.colorMode.isDark || (appSettings.colorMode.isSystem && systemDarkTheme)
    val amoledMode = appSettings.colorMode.isAmoled
    val dynamicColor = appSettings.keyColor == 0
    val colorStyle = appSettings.paletteStyle
    val colorSpec = appSettings.colorSpec

    val baseColorScheme = if (dynamicColor) {
        val baseScheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        rememberDynamicColorScheme(
            seedColor = Color.Unspecified,
            isDark = darkTheme,
            isAmoled = amoledMode,
            style = colorStyle,
            specVersion = colorSpec,
            primary = baseScheme.primary,
            secondary = baseScheme.secondary,
            tertiary = baseScheme.tertiary,
            neutral = baseScheme.surface,
            neutralVariant = baseScheme.surfaceVariant,
            error = baseScheme.error
        )
    } else {
        rememberDynamicColorScheme(
            seedColor = Color(appSettings.keyColor),
            isDark = darkTheme,
            isAmoled = amoledMode,
            style = colorStyle,
            specVersion = colorSpec,
        )
    }

    // FolkPatch-style transparency
    val useCustomBackground = BackgroundConfig.hasAnyWallpaper
    val colorScheme = if (useCustomBackground) {
        baseColorScheme.copy(
            background = Color.Transparent,
            surface = baseColorScheme.surface.copy(alpha = BackgroundConfig.cardOpacity),
            surfaceContainer = baseColorScheme.surfaceContainer.copy(alpha = BackgroundConfig.cardOpacity),
            surfaceContainerLow = baseColorScheme.surfaceContainerLow.copy(alpha = BackgroundConfig.cardOpacity),
            surfaceContainerHigh = baseColorScheme.surfaceContainerHigh.copy(alpha = BackgroundConfig.cardOpacity),
            surfaceContainerHighest = baseColorScheme.surfaceContainerHighest.copy(alpha = BackgroundConfig.cardOpacity),
            surfaceVariant = baseColorScheme.surfaceVariant.copy(alpha = BackgroundConfig.cardOpacity),
            secondaryContainer = baseColorScheme.secondaryContainer.copy(alpha = BackgroundConfig.cardOpacity),
            tertiaryContainer = baseColorScheme.tertiaryContainer.copy(alpha = BackgroundConfig.cardOpacity),
            primaryContainer = baseColorScheme.primaryContainer.copy(alpha = BackgroundConfig.cardOpacity),
        )
    } else {
        baseColorScheme
    }

    LaunchedEffect(darkTheme) {
        val window = (context as? Activity)?.window ?: return@LaunchedEffect
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }

    // M3E typography
    val baseTypography = MaterialTheme.typography
    val expressiveTypography = Typography(
        displayLarge = baseTypography.displayLarge.copy(fontWeight = FontWeight.Bold),
        displayMedium = baseTypography.displayMedium.copy(fontWeight = FontWeight.Bold),
        displaySmall = baseTypography.displaySmall.copy(fontWeight = FontWeight.SemiBold),
        headlineLarge = baseTypography.headlineLarge.copy(fontWeight = FontWeight.Bold),
        headlineMedium = baseTypography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        headlineSmall = baseTypography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = baseTypography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = baseTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        titleSmall = baseTypography.titleSmall.copy(fontWeight = FontWeight.Medium),
        bodyLarge = baseTypography.bodyLarge,
        bodyMedium = baseTypography.bodyMedium,
        bodySmall = baseTypography.bodySmall,
        labelLarge = baseTypography.labelLarge.copy(fontWeight = FontWeight.Medium),
        labelMedium = baseTypography.labelMedium.copy(fontWeight = FontWeight.Medium),
        labelSmall = baseTypography.labelSmall.copy(fontWeight = FontWeight.Medium),
    )

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        shapes = KinSUExpressiveShapes,
        motionScheme = MotionScheme.expressive(),
        typography = expressiveTypography,
        content = content,
    )
}

/**
 * Wraps content with FolkPatch-style BackgroundLayer + content layering.
 * Place this at the root of the composable tree.
 */
@Composable
fun KinSUThemeWithBackground(
    pageKey: String,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundLayer(pageKey = pageKey)
        Box(modifier = Modifier.fillMaxSize().zIndex(1f)) {
            content()
        }
    }
}
