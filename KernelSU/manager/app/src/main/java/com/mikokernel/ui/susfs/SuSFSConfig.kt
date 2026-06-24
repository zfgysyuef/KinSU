package com.mikokernel.ui.susfs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.topjohnwu.superuser.ShellUtils
import kotlinx.coroutines.launch
import com.mikokernel.R
import com.mikokernel.ui.navigation3.LocalNavigator
import com.mikokernel.ui.susfs.util.SuSFSManager
import com.mikokernel.ui.util.getRootShell
import com.mikokernel.ui.util.getSuSFSVersion
import com.mikokernel.ui.util.getSuSFSFeatures
import com.mikokernel.ui.util.getSuSFSStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuSFSConfigScreen() {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()

    var unameValue by remember { mutableStateOf("") }
    var buildTimeValue by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var sufsVersion by remember { mutableStateOf("加载中...") }
    var sufsFeatures by remember { mutableStateOf("") }
    var sufsAvailable by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                sufsVersion = getSuSFSVersion()
                sufsAvailable = sufsVersion != "unsupport"
            } catch (_: Exception) {
                sufsVersion = "不支持"
            }
            try {
                sufsFeatures = getSuSFSFeatures()
            } catch (_: Exception) {
                sufsFeatures = "N/A"
            }
            unameValue = SuSFSManager.getUnameValue(context)
            buildTimeValue = SuSFSManager.getBuildTimeValue(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("SUSFS 配置", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 状态卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (sufsAvailable) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null,
                            tint = if (sufsAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (sufsAvailable) "SUSFS 已激活" else "SUSFS 不可用", fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("版本: $sufsVersion", style = MaterialTheme.typography.bodyMedium)
                    if (sufsFeatures.isNotBlank() && sufsFeatures != "N/A") {
                        Text("功能: $sufsFeatures", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (!sufsAvailable) {
                        Spacer(Modifier.height(4.dp))
                        Text("SUSFS 需要内核源码补丁支持，当前内核未集成 SUSFS 补丁", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Uname 伪装配置
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Uname 伪装", fontWeight = FontWeight.Medium)
                    Text("自定义内核 uname 信息以隐藏 Root 检测", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    OutlinedTextField(
                        value = unameValue,
                        onValueChange = { unameValue = it },
                        label = { Text("Uname 值") },
                        placeholder = { Text("Linux version x.x.x...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = buildTimeValue,
                        onValueChange = { buildTimeValue = it },
                        label = { Text("编译时间") },
                        placeholder = { Text("#1 SMP PREEMPT ...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text("当前: ${SuSFSManager.getUnameValue(context)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("编译: ${SuSFSManager.getBuildTimeValue(context)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = {
                                unameValue = "default"
                                buildTimeValue = "default"
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("重置")
                        }
                        Button(
                            onClick = {
                                if (unameValue.isNotBlank() || buildTimeValue.isNotBlank()) {
                                    coroutineScope.launch {
                                        isLoading = true
                                        val shell = getRootShell()
                                        val finalUname = unameValue.trim().ifBlank { "default" }
                                        val finalBuild = buildTimeValue.trim().ifBlank { "default" }
                                        ShellUtils.fastCmd(shell, "ksu_susfs set_uname '$finalUname' '$finalBuild'")
                                        SuSFSManager.saveUnameValue(context, finalUname)
                                        SuSFSManager.saveBuildTimeValue(context, finalBuild)
                                        isLoading = false
                                    }
                                }
                            },
                            enabled = !isLoading && sufsAvailable,
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(stringResource(R.string.susfs_apply))
                        }
                    }
                }
            }
        }
    }
}
