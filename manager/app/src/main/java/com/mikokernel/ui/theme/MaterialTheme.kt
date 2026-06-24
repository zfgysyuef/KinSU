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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import com.materialkolor.rememberDynamicColorScheme

/**
 * iPhone-style typography: slightly larger sizes, more weight contrast,
 * mimicking San Francisco / SF Pro characteristics.
 */
private val iPhoneTypography = Typography(
    displayLarge = Typography().displayLarge.copy(fontSize = 57.sp, fontWeight = FontWeight.Bold),
    displayMedium = Typography().displayMedium.copy(fontSize = 45.sp, fontWeight = FontWeight.SemiBold),
    displaySmall = Typography().displaySmall.copy(fontSize = 36.sp, fontWeight = FontWeight.Medium),
    headlineLarge = Typography().headlineLarge.copy(fontSize = 32.sp, fontWeight = FontWeight.SemiBold),
    headlineMedium = Typography().headlineMedium.copy(fontSize = 28.sp, fontWeight = FontWeight.SemiBold),
    headlineSmall = Typography().headlineSmall.copy(fontSize = 24.sp, fontWeight = FontWeight.Medium),
    titleLarge = Typography().titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = Typography().titleMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.01).sp),
    titleSmall = Typography().titleSmall.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.01).sp),
    bodyLarge = Typography().bodyLarge.copy(fontSize = 16.sp, letterSpacing = (-0.02).sp),
    bodyMedium = Typography().bodyMedium.copy(fontSize = 14.sp, letterSpacing = (-0.02).sp),
    bodySmall = Typography().bodySmall.copy(fontSize = 12.sp, letterSpacing = (-0.03).sp),
    labelLarge = Typography().labelLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.04).sp),
    labelMedium = Typography().labelMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.05).sp),
    labelSmall = Typography().labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.06).sp),
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

    val typography = when (appSettings.fontMode) {
        FontMode.IPHONE -> iPhoneTypography
        else -> Typography()
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = typography,
        motionScheme = MotionScheme.expressive(),
        content = content,
    )
}
