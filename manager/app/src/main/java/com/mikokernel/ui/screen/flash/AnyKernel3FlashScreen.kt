package com.mikokernel.ui.screen.flash

import android.net.Uri
import android.os.Environment
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mikokernel.R
import com.mikokernel.ui.navigation3.LocalNavigator
import com.mikokernel.ui.util.AnyKernel3Worker
import com.mikokernel.ui.util.install
import com.mikokernel.ui.util.reboot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnyKernel3FlashScreen(kernelUri: String, slot: String? = null) {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val scrollState = rememberScrollState()

    val logs = remember { mutableStateListOf<String>() }
    var progress by remember { mutableStateOf(0f) }
    val preparingStep = stringResource(R.string.anykernel3_preparing)
    var currentStep by remember { mutableStateOf(preparingStep) }
    var isComplete by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var showFab by remember { mutableStateOf(false) }
    val logContent = remember { StringBuilder() }

    fun saveLog() {
        scope.launch {
            val fmt = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault())
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "KinSU_flash_${fmt.format(Date())}.log")
            file.writeText(logContent.toString())
        }
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val uri = Uri.parse(kernelUri)
            val worker = AnyKernel3Worker(
                context = context,
                kernelUri = uri,
                selectedSlot = slot,
                onLog = { entry ->
                    logs.add(entry.text)
                    logContent.appendLine(entry.text)
                    progress = entry.progress
                    if (entry.step.isNotEmpty()) currentStep = entry.step
                },
                onComplete = {
                    isComplete = true; progress = 1f; showFab = true
                    try { install() } catch (_: Exception) {}
                },
                onError = { err ->
                    errorMsg = err; logs.add("ERROR: $err")
                    logContent.appendLine("ERROR: $err")
                }
            )
            worker.start()
            worker.join()
        }
    }

    val fabText = stringResource(R.string.reboot)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            errorMsg.isNotEmpty() -> stringResource(R.string.flash_failed)
                            isComplete -> stringResource(R.string.flash_success)
                            else -> stringResource(R.string.horizon_kernel)
                        },
                        color = when {
                            errorMsg.isNotEmpty() -> MaterialTheme.colorScheme.error
                            isComplete -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { saveLog() }) {
                        Icon(Icons.Filled.Save, stringResource(R.string.save_log), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            if (showFab) {
                ExtendedFloatingActionButton(
                    onClick = { scope.launch(Dispatchers.IO) { reboot() } },
                    icon = { Icon(Icons.Filled.Refresh, fabText) },
                    text = { Text(fabText) },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    expanded = true
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            FlashCard(progress, currentStep, isComplete, errorMsg)
            Box(Modifier.fillMaxWidth().weight(1f).verticalScroll(scrollState)) {
                LaunchedEffect(logs.size) { scrollState.animateScrollTo(scrollState.maxValue) }
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = logs.joinToString("\n"),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun FlashCard(progress: Float, step: String, isComplete: Boolean, error: String) {
    val progressColor = when {
        error.isNotEmpty() -> MaterialTheme.colorScheme.error
        isComplete -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    val animProgress = animateFloatAsState(progress, label = "p").value
    Card(Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    when { error.isNotEmpty() -> stringResource(R.string.flash_failed); isComplete -> stringResource(R.string.flash_success); else -> stringResource(R.string.flashing) },
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = progressColor
                )
                when {
                    error.isNotEmpty() -> Icon(Icons.Filled.Error, null, tint = MaterialTheme.colorScheme.error)
                    isComplete -> Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.tertiary)
                }
            }
            if (step.isNotEmpty()) { Spacer(Modifier.height(8.dp)); Text(step, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(8.dp)) }
            LinearWavyProgressIndicator({ animProgress }, Modifier.fillMaxWidth().height(8.dp), color = progressColor, trackColor = MaterialTheme.colorScheme.surfaceVariant)
            if (error.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(error, Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.errorContainer.copy(0.3f), MaterialTheme.shapes.small).padding(8.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
