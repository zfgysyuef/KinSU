package com.mikokernel.ui.screen.kpm

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.mikokernel.kpm.KpmMode
import com.mikokernel.ui.navigation3.LocalNavigator
import com.mikokernel.ui.util.kpmGetVersion
import com.mikokernel.ui.util.kpmListModules
import com.mikokernel.ui.util.kpmInstallModule
import com.mikokernel.ui.util.kpmToggleModule
import com.mikokernel.ui.util.kpmRunModuleAction
import com.mikokernel.ui.util.kpmUninstallModule
import org.json.JSONArray

data class KpmModuleInfo(
    val id: String, val name: String, val version: String = "",
    val enabled: Boolean = true, val hasWeb: Boolean = false, val hasAction: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KpmScreen() {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var kpmModules by remember { mutableStateOf<List<KpmModuleInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var kpmAvailable by remember { mutableStateOf(KpmMode.isActive()) }
    var isFlashing by remember { mutableStateOf(false) }
    var flashLog by remember { mutableStateOf("") }
    var showFlashDialog by remember { mutableStateOf(false) }

    fun showMessage(msg: String) { scope.launch { snackbarHostState.showSnackbar(msg) } }

    fun refreshModules() {
        scope.launch {
            isLoading = true
            withContext(Dispatchers.IO) {
                try {
                    val json = kpmListModules()
                    if (json.isNotBlank() && json != "[]") {
                        val arr = JSONArray(json)
                        kpmModules = (0 until arr.length()).map { i ->
                            val obj = arr.getJSONObject(i)
                            KpmModuleInfo(
                                id = obj.optString("id", "?"), name = obj.optString("name", obj.optString("id", "?")),
                                version = obj.optString("version", ""), enabled = obj.optString("enabled", "true") == "true",
                                hasWeb = obj.optString("web", "false") == "true", hasAction = obj.optString("action", "false") == "true",
                            )
                        }
                    } else { kpmModules = emptyList() }
                } catch (_: Exception) { kpmModules = emptyList() }
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val version = kpmGetVersion()
                if (version.isNotBlank() && version != "unsupported") {
                    kpmAvailable = true
                    if (!KpmMode.isActive()) {
                        KpmMode.activate()
                        KpmMode.persist(context)
                    }
                } else {
                    kpmAvailable = KpmMode.isActive()
                }
            } catch (_: Exception) {
                kpmAvailable = KpmMode.isActive()
            }
        }
        if (kpmAvailable) refreshModules() else { isLoading = false }
    }

    val selectZipLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) result.data?.data?.let { uri ->
            isFlashing = true
            flashLog = ""
            showFlashDialog = true
            scope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        val result = kpmInstallModule(uri,
                            { line -> flashLog += line + "\n" },
                            { line -> flashLog += "[E] $line\n" }
                        )
                        if (result.code == 0 && !flashLog.contains("Error:")) {
                            flashLog += "\n--- 刷写成功 ---"
                        } else {
                            flashLog += "\n--- 刷写失败 (code=${result.code}) ---\n提示：请确保已通过「修补内核并刷入」启用 GKI/KPM 模式"
                        }
                    } catch (e: Exception) {
                        flashLog += "\n--- 刷写失败: ${e.message} ---"
                    }
                }
                isFlashing = false
                refreshModules()
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("KPM 模块管理") },
                navigationIcon = { IconButton(onClick = dropUnlessResumed { navigator.pop() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { refreshModules() }) { Icon(Icons.Filled.Refresh, "刷新") }
                    IconButton(onClick = {
                        selectZipLauncher.launch(Intent(Intent.ACTION_GET_CONTENT).apply {
                            type = "*/*"
                            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/zip", "application/octet-stream"))
                        })
                    }) { Icon(Icons.Filled.Add, "安装 KPM") }
                })
        }) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            // 状态卡片
            Card(Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (kpmAvailable) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (kpmAvailable) Icons.Filled.CheckCircle else Icons.Filled.Cancel, null,
                        tint = if (kpmAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(12.dp))
                    Text(if (kpmAvailable) "KPM 可用" else "KPM 不可用，请先修补内核并刷入", style = MaterialTheme.typography.titleMedium)
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (kpmModules.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Extension, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(16.dp))
                        Text("暂无 KPM 模块", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(8.dp))
                        Text("点击右上角 + 安装 KPM 模块", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(kpmModules) { mod ->
                        var showUninstall by remember { mutableStateOf(false) }
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(mod.name, style = MaterialTheme.typography.titleSmall)
                                        Text("v${mod.version}  ${mod.id}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Switch(checked = mod.enabled, onCheckedChange = { en ->
                                        scope.launch { withContext(Dispatchers.IO) { kpmToggleModule(mod.id, en) }; refreshModules() }
                                    })
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    if (mod.hasAction) TextButton(onClick = {
                                        scope.launch { withContext(Dispatchers.IO) { kpmRunModuleAction(mod.id, {}, {}) }; showMessage("已执行: ${mod.name}") }
                                    }) { Text("执行") }
                                    TextButton(onClick = { showUninstall = true }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("卸载") }
                                }
                            }
                        }
                        if (showUninstall) AlertDialog(onDismissRequest = { showUninstall = false }, title = { Text("卸载 ${mod.name}?") }, confirmButton = {
                            TextButton(onClick = { showUninstall = false; scope.launch { withContext(Dispatchers.IO) { kpmUninstallModule(mod.id) }; refreshModules() } }) { Text("卸载", color = MaterialTheme.colorScheme.error) }
                        }, dismissButton = { TextButton(onClick = { showUninstall = false }) { Text("取消") } })
                    }
                }
            }
        }
    }

    // 刷写日志对话框
    if (showFlashDialog) {
        AlertDialog(
            onDismissRequest = { if (!isFlashing) showFlashDialog = false },
            title = { Text(if (isFlashing) "正在刷写..." else "刷写完成") },
            text = {
                Column(Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                    if (isFlashing) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                    }
                    Text(flashLog, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                if (!isFlashing) {
                    TextButton(onClick = { showFlashDialog = false }) { Text("关闭") }
                }
            }
        )
    }
}
