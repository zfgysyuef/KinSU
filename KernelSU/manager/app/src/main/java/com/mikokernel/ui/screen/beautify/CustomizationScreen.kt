package com.mikokernel.ui.screen.beautify

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mikokernel.ui.component.material.SegmentedColumn
import com.mikokernel.ui.component.material.SegmentedListItem
import com.mikokernel.ui.component.material.SegmentedSwitchItem
import com.mikokernel.ui.component.material.TonalCard
import com.mikokernel.ui.theme.beautify.BackgroundConfig
import java.io.File
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    var moduleId by rememberSaveable { mutableStateOf("") }
    val trimmedModuleId = moduleId.trim()

    LaunchedEffect(Unit) {
        BackgroundConfig.load(context)
    }

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("美化与壁纸") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BeautifyPreviewCard(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                globalEnabled = BackgroundConfig.isGlobalEnabled,
                hasGlobalWallpaper = BackgroundConfig.globalUri != null,
                cardOpacity = BackgroundConfig.cardOpacity,
                cardHeightScale = BackgroundConfig.cardHeightScale
            )

            SegmentedColumn(
                title = "全局壁纸",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                content = listOf(
                    {
                        SegmentedSwitchItem(
                            icon = Icons.Filled.Wallpaper,
                            title = "启用整体壁纸",
                            summary = if (BackgroundConfig.globalUri == null) "先选择图片，选择后会自动启用" else "所有页面共用同一张背景图",
                            checked = BackgroundConfig.isGlobalEnabled,
                            onCheckedChange = { BackgroundConfig.setGlobalEnabled(context, it) }
                        )
                    },
                    {
                        ImagePickerItem(
                            title = "整体壁纸",
                            summary = "作为管理器背景显示",
                            storageKey = "global",
                            currentUri = BackgroundConfig.globalUri,
                            onPick = { BackgroundConfig.setGlobal(context, it) },
                            onClear = { BackgroundConfig.setGlobal(context, null) }
                        )
                    }
                )
            )

            SegmentedColumn(
                title = "首页卡片",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                content = listOf(
                    { CardBackgroundPicker(title = "工作状态", cardId = "home:status") },
                    { CardBackgroundPicker(title = "系统信息", cardId = "home:info") },
                    { CardBackgroundPicker(title = "更新提示", cardId = "home:update") },
                    { CardBackgroundPicker(title = "支持项目", cardId = "home:donate") },
                    { CardBackgroundPicker(title = "了解更多", cardId = "home:learn_more") }
                )
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "模块卡片",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp)
                )
                OutlinedTextField(
                    value = moduleId,
                    onValueChange = { moduleId = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("模块 ID") },
                    supportingText = { Text("输入模块 id 后，只修改对应模块卡片") }
                )
                if (trimmedModuleId.isNotEmpty()) {
                    SegmentedColumn(
                        content = listOf(
                            {
                                CardBackgroundPicker(
                                    title = trimmedModuleId,
                                    cardId = BackgroundConfig.moduleCardId(trimmedModuleId)
                                )
                            }
                        )
                    )
                }
            }

            SegmentedColumn(
                title = "显示效果",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                content = buildList {
                    add {
                        SliderItem(
                            title = "卡片透明度",
                            value = BackgroundConfig.cardOpacity,
                            valueText = "${(BackgroundConfig.cardOpacity * 100).roundToInt()}%",
                            valueRange = 0.35f..1f,
                            onValueChange = { BackgroundConfig.setCardOpacity(context, it) }
                        )
                    }
                    add {
                        SliderItem(
                            title = "卡片高度比例",
                            value = BackgroundConfig.cardHeightScale,
                            valueText = "${(BackgroundConfig.cardHeightScale * 100).roundToInt()}%",
                            valueRange = 0.85f..1.45f,
                            onValueChange = { BackgroundConfig.setCardHeightScale(context, it) }
                        )
                    }
                    add {
                        SegmentedSwitchItem(
                            icon = Icons.Filled.Tune,
                            title = "日夜暗度分离",
                            summary = "亮色更清透，深色自动压暗",
                            checked = BackgroundConfig.isDualBackgroundDimEnabled,
                            onCheckedChange = { BackgroundConfig.setDualBackgroundDimEnabled(context, it) }
                        )
                    }
                    if (BackgroundConfig.isDualBackgroundDimEnabled) {
                        add {
                            SliderItem(
                                title = "日间壁纸暗度",
                                value = BackgroundConfig.backgroundDayDim,
                                valueText = "${(BackgroundConfig.backgroundDayDim * 100).roundToInt()}%",
                                valueRange = 0f..0.75f,
                                onValueChange = { BackgroundConfig.setBackgroundDayDim(context, it) }
                            )
                        }
                        add {
                            SliderItem(
                                title = "夜间壁纸暗度",
                                value = BackgroundConfig.backgroundNightDim,
                                valueText = "${(BackgroundConfig.backgroundNightDim * 100).roundToInt()}%",
                                valueRange = 0.15f..0.85f,
                                onValueChange = { BackgroundConfig.setBackgroundNightDim(context, it) }
                            )
                        }
                    } else {
                        add {
                            SliderItem(
                                title = "背景暗度",
                                value = BackgroundConfig.backgroundDim,
                                valueText = "${(BackgroundConfig.backgroundDim * 100).roundToInt()}%",
                                valueRange = 0f..0.85f,
                                onValueChange = { BackgroundConfig.setBackgroundDim(context, it) }
                            )
                        }
                    }
                    add {
                        SliderItem(
                            title = "卡片图片透明度",
                            value = BackgroundConfig.cardBgOpacity,
                            valueText = "${(BackgroundConfig.cardBgOpacity * 100).roundToInt()}%",
                            valueRange = 0.2f..1f,
                            onValueChange = { BackgroundConfig.setCardBgOpacity(context, it) }
                        )
                    }
                    add {
                        SliderItem(
                            title = "卡片背景暗度",
                            value = BackgroundConfig.cardBgDim,
                            valueText = "${(BackgroundConfig.cardBgDim * 100).roundToInt()}%",
                            valueRange = 0.18f..0.78f,
                            onValueChange = { BackgroundConfig.setCardBgDim(context, it) }
                        )
                    }
                    add {
                        SliderItem(
                            title = "背景模糊",
                            value = BackgroundConfig.backgroundBlur,
                            valueText = "${BackgroundConfig.backgroundBlur.roundToInt()} dp",
                            valueRange = 0f..25f,
                            onValueChange = { BackgroundConfig.setBackgroundBlur(context, it) }
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BeautifyPreviewCard(
    modifier: Modifier = Modifier,
    globalEnabled: Boolean,
    hasGlobalWallpaper: Boolean,
    cardOpacity: Float,
    cardHeightScale: Float,
) {
    TonalCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        Icons.Filled.Image,
                        contentDescription = null,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "当前外观",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (globalEnabled && hasGlobalWallpaper) "整体壁纸已启用" else "使用主题色背景",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((96.dp * cardHeightScale).coerceIn(82.dp, 140.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.24f),
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.88f)
                            )
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 1f - cardOpacity.coerceIn(0.35f, 1f)))
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(horizontal = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "模块与主页卡片会保持文字优先",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "图片做背景，内容保持清楚可读",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CardBackgroundPicker(
    title: String,
    cardId: String
) {
    val context = LocalContext.current
    ImagePickerItem(
        title = title,
        summary = "单独保存，不影响其他卡片",
        storageKey = cardId,
        currentUri = BackgroundConfig.getCardBackground(cardId),
        onPick = { BackgroundConfig.setCardBackground(context, cardId, it) },
        onClear = { BackgroundConfig.clearCardBackground(context, cardId) }
    )
}

@Composable
private fun ImagePickerItem(
    title: String,
    summary: String,
    storageKey: String,
    currentUri: String?,
    onPick: (String) -> Unit,
    onClear: () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            val storedUri = savePickedImage(context, it, storageKey, currentUri)
            onPick(storedUri ?: it.toString())
        }
    }

    SegmentedListItem(
        onClick = { launcher.launch(arrayOf("image/*")) },
        headlineContent = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            Text(if (currentUri.isNullOrEmpty()) "$summary · 未设置" else "$summary · 已设置")
        },
        leadingContent = { Icon(Icons.Filled.Wallpaper, title) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { launcher.launch(arrayOf("image/*")) }) {
                    Icon(Icons.Filled.Image, "选择图片")
                }
                if (!currentUri.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = {
                        deleteStoredImage(currentUri)
                        onClear()
                    }) {
                        Icon(Icons.Filled.Delete, "清除图片")
                    }
                }
            }
        }
    )
}

private fun savePickedImage(context: Context, sourceUri: Uri, key: String, previousUri: String?): String? {
    val safeKey = key.replace(Regex("[^A-Za-z0-9_.-]"), "_")
    val dir = File(context.filesDir, "beautify/backgrounds").apply { mkdirs() }
    val target = File(dir, "$safeKey-${System.currentTimeMillis()}.img")
    return try {
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        if (!previousUri.isNullOrEmpty()) deleteStoredImage(previousUri)
        Uri.fromFile(target).toString()
    } catch (_: Exception) {
        null
    }
}

private fun deleteStoredImage(uri: String) {
    runCatching {
        val file = Uri.parse(uri).path?.let(::File) ?: return
        if (file.absolutePath.contains("${File.separator}beautify${File.separator}backgrounds${File.separator}")) {
            file.delete()
        }
    }
}

@Composable
private fun SliderItem(
    title: String,
    value: Float,
    valueText: String,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    SegmentedListItem(
        headlineContent = { Text(title) },
        leadingContent = { Icon(Icons.Filled.Tune, title) },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(valueText, color = MaterialTheme.colorScheme.outline)
                Slider(
                    value = value.coerceIn(valueRange.start, valueRange.endInclusive),
                    onValueChange = onValueChange,
                    valueRange = valueRange
                )
            }
        }
    )
}
