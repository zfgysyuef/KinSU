package com.mikokernel.ui.theme.beautify

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mikokernel.ui.component.material.TonalCard
import java.io.File

/**
 * Smart card that shows backgrounds and adjusts opacity when wallpapers are active.
 * Supports per-card background images via cardBgUri parameter.
 */
@Composable
fun BeautifyCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer,
    cardId: String? = null,
    cardBgUri: String? = null,
    cardBgAlpha: Float = -1f,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val hasWallpaper = BackgroundConfig.isGlobalEnabled ||
            BackgroundConfig.homeUri != null ||
            BackgroundConfig.superuserUri != null ||
            BackgroundConfig.moduleUri != null ||
            BackgroundConfig.settingsUri != null

    val effectiveColor = if (hasWallpaper) {
        containerColor.copy(alpha = BackgroundConfig.cardOpacity)
    } else {
        containerColor
    }

    val resolvedBgUri = cardBgUri ?: cardId?.let { BackgroundConfig.getCardBackground(it) }
    val requestedAlpha = if (cardBgAlpha < 0f) BackgroundConfig.cardBgOpacity else cardBgAlpha
    val resolvedAlpha = if (resolvedBgUri != null) requestedAlpha.coerceIn(0f, 1f) else 0f
    val baseMinHeight = when {
        cardId == "home:status" -> 116.dp
        cardId == "home:info" -> 0.dp
        cardId?.startsWith("home:") == true -> 88.dp
        cardId?.startsWith("module:") == true -> 104.dp
        else -> 0.dp
    }
    val minHeight = if (baseMinHeight > 0.dp) baseMinHeight * BackgroundConfig.cardHeightScale else 0.dp
    val cardModifier = if (minHeight > 0.dp) {
        modifier.heightIn(min = minHeight)
    } else {
        modifier
    }
    val contentModifier = if (minHeight > 0.dp) {
        Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
    } else {
        Modifier.fillMaxWidth()
    }

    var cardBgBitmap by remember(resolvedBgUri) {
        mutableStateOf(
            resolvedBgUri?.let { uriStr -> loadBitmap(context, uriStr) }
        )
    }
    val hasCardBackground = cardBgBitmap != null && resolvedAlpha > 0f

    val cardContent = @Composable {
        Box(modifier = contentModifier) {
            if (hasCardBackground) {
                Image(
                    painter = BitmapPainter(cardBgBitmap!!.asImageBitmap()),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    alpha = resolvedAlpha
                )
                val dimAlpha = BackgroundConfig.cardBgDim.coerceIn(0.18f, 0.78f)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = dimAlpha))
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.08f),
                                    Color.Black.copy(alpha = 0.36f)
                                )
                            )
                        )
                )
            }
            if (hasCardBackground) {
                CompositionLocalProvider(LocalContentColor provides Color.White) {
                    content()
                }
            } else {
                content()
            }
        }
    }
    if (onClick != null) {
        TonalCard(
            modifier = cardModifier,
            containerColor = effectiveColor,
            onClick = onClick,
            content = cardContent
        )
    } else {
        TonalCard(
            modifier = cardModifier,
            containerColor = effectiveColor,
            content = cardContent
        )
    }
}

private fun loadBitmap(context: android.content.Context, uriStr: String) = try {
    val uri = Uri.parse(uriStr)
    if (uri.scheme == "file") {
        uri.path?.let { path ->
            File(path).inputStream().use { BitmapFactory.decodeStream(it) }
        }
    } else {
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
    }
} catch (_: Exception) {
    null
}
