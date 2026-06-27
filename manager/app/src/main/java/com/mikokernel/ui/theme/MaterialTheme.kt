package com.mikokernel.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import com.materialkolor.rememberDynamicColorScheme

// M3E 排版：更大胆的字重对比，更紧凑的字间距
private val KinSUTypography = Typography(
    displayLarge = Typography().displayLarge.copy(fontWeight = FontWeight.Bold),
    displayMedium = Typography().displayMedium.copy(fontWeight = FontWeight.SemiBold),
    displaySmall = Typography().displaySmall.copy(fontWeight = FontWeight.SemiBold),
    headlineLarge = Typography().headlineLarge.copy(fontWeight = FontWeight.SemiBold),
    headlineMedium = Typography().headlineMedium.copy(fontWeight = FontWeight.SemiBold),
    headlineSmall = Typography().headlineSmall.copy(fontWeight = FontWeight.Medium),
    titleLarge = Typography().titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = Typography().titleMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.1).sp),
    titleSmall = Typography().titleSmall.copy(fontWeight = FontWeight.Medium, letterSpacing = (-0.1).sp),
    bodyLarge = Typography().bodyLarge.copy(letterSpacing = (-0.15).sp),
    bodyMedium = Typography().bodyMedium.copy(letterSpacing = (-0.15).sp),
    bodySmall = Typography().bodySmall.copy(letterSpacing = (-0.2).sp),
    labelLarge = Typography().labelLarge.copy(fontWeight = FontWeight.Medium, letterSpacing = (-0.25).sp),
    labelMedium = Typography().labelMedium.copy(fontWeight = FontWeight.Medium),
    labelSmall = Typography().labelSmall.copy(fontWeight = FontWeight.Medium),
)

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

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        shapes = KinSUShapes,
        typography = KinSUTypography,
        motionScheme = MotionScheme.expressive(),
        content = content,
    )
}
