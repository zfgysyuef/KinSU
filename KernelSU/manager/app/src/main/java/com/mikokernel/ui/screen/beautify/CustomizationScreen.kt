package com.mikokernel.ui.screen.beautify

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mikokernel.ui.theme.beautify.BackgroundConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizationScreen(onBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val appCtx = LocalContext.current

    LaunchedEffect(Unit) { BackgroundConfig.load(appCtx) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Customization") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionTitle("Global Wallpaper")
            WallpaperPicker(
                label = "Background (all pages)",
                currentUri = if (BackgroundConfig.isGlobalEnabled) BackgroundConfig.globalUri else null,
                onPick = { uri -> BackgroundConfig.setGlobal(appCtx, uri) },
                onClear = { BackgroundConfig.setGlobal(appCtx, null) }
            )

            Spacer(Modifier.height(4.dp))

            SectionTitle("Per-Page Wallpapers")
            WallpaperPicker("Home", BackgroundConfig.homeUri,
                { BackgroundConfig.setHome(appCtx, it) },
                { BackgroundConfig.clearPage(appCtx, BackgroundConfig.KEY_HOME) })
            WallpaperPicker("Superuser", BackgroundConfig.superuserUri,
                { BackgroundConfig.setSuperuser(appCtx, it) },
                { BackgroundConfig.clearPage(appCtx, BackgroundConfig.KEY_SUPERUSER) })
            WallpaperPicker("Module", BackgroundConfig.moduleUri,
                { BackgroundConfig.setModule(appCtx, it) },
                { BackgroundConfig.clearPage(appCtx, BackgroundConfig.KEY_MODULE) })
            WallpaperPicker("Settings", BackgroundConfig.settingsUri,
                { BackgroundConfig.setSettings(appCtx, it) },
                { BackgroundConfig.clearPage(appCtx, BackgroundConfig.KEY_SETTINGS) })

            Spacer(Modifier.height(4.dp))

            SectionTitle("Appearance")
            SliderSetting("Card Opacity", BackgroundConfig.cardOpacity, 0.1f..1f) {
                BackgroundConfig.setCardOpacity(appCtx, it)
            }
            SliderSetting("Background Dim", BackgroundConfig.backgroundDim, 0f..0.85f) {
                BackgroundConfig.setBackgroundDim(appCtx, it)
            }
            SliderSetting("Background Blur", BackgroundConfig.backgroundBlur, 0f..25f) {
                BackgroundConfig.setBackgroundBlur(appCtx, it)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun SliderSetting(
    label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit
) {
    var v by remember { mutableFloatStateOf(value) }
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("%.0f%%".format(v * 100), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
        Slider(
            value = v, onValueChange = { v = it }, onValueChangeFinished = { onChange(v) },
            valueRange = range,
            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
private fun WallpaperPicker(
    label: String, currentUri: String?, onPick: (String) -> Unit, onClear: () -> Unit
) {
    val ctx = LocalContext.current
    var bitmap by remember(currentUri) { mutableStateOf(currentUri?.let { loadBmp(ctx, it) }) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try { ctx.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            catch (_: Exception) {}
            onPick(uri.toString())
            bitmap = loadBmp(ctx, uri.toString())
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            if (bitmap != null) {
                Box(Modifier.fillMaxWidth().height(72.dp).clip(RoundedCornerShape(8.dp))) {
                    Image(bitmap = bitmap!!, contentDescription = label, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f)))))
                }
            } else {
                Box(
                    Modifier.fillMaxWidth().height(72.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Outlined.BrokenImage, null, tint = MaterialTheme.colorScheme.outline) }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                if (currentUri != null) {
                    IconButton(onClick = { onClear(); bitmap = null }) { Icon(Icons.Outlined.Delete, "Clear") }
                    Spacer(Modifier.width(8.dp))
                }
                TextButton(onClick = { launcher.launch("image/*") }) {
                    Icon(Icons.Outlined.Edit, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (currentUri != null) "Change" else "Set Wallpaper")
                }
            }
        }
    }
}

private fun loadBmp(ctx: android.content.Context, uri: String): ImageBitmap? = try {
    ctx.contentResolver.openInputStream(Uri.parse(uri))?.use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
} catch (_: Exception) { null }