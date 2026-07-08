package com.mikokernel.ui.theme.beautify

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File

/**
 * Full-screen wallpaper layer rendered behind all content.
 * Place this as the first child inside a Box; content goes on top with zIndex(1f).
 *
 * @param pageKey semantic key from BackgroundConfig (e.g. KEY_HOME)
 */
@Composable
fun BackgroundLayer(pageKey: String) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    // Load config on first composition
    LaunchedEffect(Unit) { BackgroundConfig.load(context) }

    val uri = remember(pageKey, BackgroundConfig.globalUri, BackgroundConfig.isGlobalEnabled,
        BackgroundConfig.homeUri, BackgroundConfig.superuserUri,
        BackgroundConfig.moduleUri, BackgroundConfig.settingsUri) {
        BackgroundConfig.resolveUri(pageKey)
    }

    if (uri == null) return

    val fallbackColor = if (isDark) Color.Black else Color.White
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(fallbackColor)
    )

    var bitmap by remember(uri) { mutableStateOf(loadBitmap(context, uri)) }
    if (bitmap == null) return

    // Background image
    Image(
        bitmap = bitmap!!.asImageBitmap(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && BackgroundConfig.backgroundBlur > 0f)
                    Modifier.blur(BackgroundConfig.backgroundBlur.dp)
                else Modifier
            )
    )

    // Dim overlay
    val dimAlpha = BackgroundConfig.effectiveBackgroundDim(isDark)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = dimAlpha))
    )
}

private fun loadBitmap(context: android.content.Context, uriStr: String) = try {
    val uri = Uri.parse(uriStr)
    if (uri.scheme == "file") {
        uri.path?.let { path ->
            File(path).inputStream().use { BitmapFactory.decodeStream(it) }
        }
    } else {
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it)
        }
    }
} catch (_: Exception) { null }
