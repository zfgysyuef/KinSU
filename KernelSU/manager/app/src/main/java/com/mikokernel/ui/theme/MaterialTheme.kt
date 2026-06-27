package com.mikokernel.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import com.materialkolor.rememberDynamicColorScheme

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

    val colorScheme = if (dynamicColor) {
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

    LaunchedEffect(darkTheme) {
        val window = (context as? Activity)?.window ?: return@LaunchedEffect
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }

    // 使用 MaterialExpressiveTheme 以获得 MotionScheme.expressive()（Switch 回弹等表现力动画），
    // 但保持标准 M3 shapes 和 typography，视觉仍是标准 Material 3。
    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        shapes = KinSUShapes,
        motionScheme = MotionScheme.expressive(),
        content = content,
    )
}

/**
 * Material 3 Expressive 主题变体。
 * 在标准 M3 配色基础上使用 MaterialExpressiveTheme，启用 expressive 形状、
 * 表现力更强的 motion scheme，以及略加重的 typography 字重，呈现更具表现力的视觉风格。
 */
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

    val colorScheme = if (dynamicColor) {
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

    LaunchedEffect(darkTheme) {
        val window = (context as? Activity)?.window ?: return@LaunchedEffect
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }

    // M3E 专属 typography：在默认 typography 基础上略加重字重、收紧字距，强化表现力
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
