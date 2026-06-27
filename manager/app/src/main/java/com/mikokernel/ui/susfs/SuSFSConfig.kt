package com.mikokernel.ui.susfs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ShellUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.mikokernel.ui.navigation3.LocalNavigator
import com.mikokernel.ui.util.getRootShell

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuSFSConfigScreen() {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }

    var isLoading by remember { mutableStateOf(false) }
    var susfsAvailable by remember { mutableStateOf(false) }
    var susfsVersion by remember { mutableStateOf("加载中...") }
    var susfsFeatures by remember { mutableStateOf("") }
    var susfsVariant by remember { mutableStateOf("") }

    // Uname
    var unameRelease by remember { mutableStateOf("default") }
    var unameVersion by remember { mutableStateOf("default") }

    // 日志
    var logEnabled by remember { mutableStateOf(false) }
    var hideMntsForNonSu by remember { mutableStateOf(false) }

    // 输入框状态
    var susPathInput by remember { mutableStateOf("") }
    var susMountInput by remember { mutableStateOf("") }
    var tryUmountPath by remember { mutableStateOf("") }
    var tryUmountMode by remember { mutableStateOf("0") }
    var susMapInput by remember { mutableStateOf("") }
    var susProcFdOriginal by remember { mutableStateOf("") }
    var susProcFdSpoofed by remember { mutableStateOf("") }
    var susMemfdInput by remember { mutableStateOf("") }
    var openRedirectTarget by remember { mutableStateOf("") }
    var openRedirectDest by remember { mutableStateOf("") }
    var openRedirectScheme by remember { mutableStateOf("0") }
    var cmdlineInput by remember { mutableStateOf("") }
    var kstatPath by remember { mutableStateOf("") }

    // 运行结果列表
    var susPaths by remember { mutableStateOf(listOf<String>()) }
    var susMounts by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val shell = getRootShell()
                    val ver = ShellUtils.fastCmd(shell, "ksu_susfs show version 2>/dev/null").trim()
                    susfsVersion = if (ver.isNotBlank()) ver else "unsupport"
                    susfsAvailable = susfsVersion != "unsupport" && susfsVersion != ""

                    val variant = ShellUtils.fastCmd(shell, "ksu_susfs show variant 2>/dev/null").trim()
                    susfsVariant = variant

                    val features = ShellUtils.fastCmd(shell, "ksu_susfs show enabled_features 2>/dev/null").trim()
                    susfsFeatures = features

                    val logStat = ShellUtils.fastCmd(shell, "ksu_susfs enable_log 2>/dev/null").trim()
                    logEnabled = logStat.contains("1") || logStat.contains("enabled")
                } catch (_: Exception) {
                    susfsVersion = "不支持"
                    susfsAvailable = false
                }
            }
        }
    }

    fun execSusfs(cmd: String, onSuccess: String? = null, onError: String? = null) {
        coroutineScope.launch {
            isLoading = true
            val result = withContext(Dispatchers.IO) {
                val shell = getRootShell()
                val r = shell.newJob().add(cmd).exec()
                Triple(r.isSuccess, r.out.joinToString("\n"), r.err.joinToString("\n"))
            }
            isLoading = false
            val (success, out, err) = result
            if (success) {
                snackbarHost.showSnackbar(onSuccess ?: "执行成功")
            } else {
                snackbarHost.showSnackbar(onError ?: "执行失败: $err")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Pets, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("SuSFS", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 状态概览
            SuSFSStatusCard(
                available = susfsAvailable,
                version = susfsVersion,
                features = susfsFeatures,
                variant = susfsVariant
            )

            if (!susfsAvailable) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "SuSFS 需要内核补丁支持，当前内核未集成 SuSFS",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                return@Scaffold
            }

            // Uname 伪装
            SuSFSSectionCard(
                icon = Icons.Filled.Info,
                title = "Uname 伪装",
                subtitle = "伪造内核版本信息，隐藏 Root 检测"
            ) {
                OutlinedTextField(
                    value = unameRelease,
                    onValueChange = { unameRelease = it },
                    label = { Text("Release (内核版本)") },
                    placeholder = { Text("如 5.15.148-android14-8") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = unameVersion,
                    onValueChange = { unameVersion = it },
                    label = { Text("Version (编译信息)") },
                    placeholder = { Text("如 #1 SMP PREEMPT ...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            unameRelease = "default"
                            unameVersion = "default"
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("重置") }
                    Button(
                        onClick = {
                            execSusfs(
                                "ksu_susfs set_uname '$unameRelease' '$unameVersion'",
                                "Uname 伪装已应用"
                            )
                        },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) { Text("应用") }
                }
            }

            // SUS Path 隐藏
            SuSFSSectionCard(
                icon = Icons.Filled.FolderOff,
                title = "SUS Path 隐藏",
                subtitle = "隐藏指定路径，使其对系统调用返回 ENOENT"
            ) {
                OutlinedTextField(
                    value = susPathInput,
                    onValueChange = { susPathInput = it },
                    label = { Text("路径") },
                    placeholder = { Text("/data/adb/modules/xxx") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            execSusfs(
                                "ksu_susfs add_sus_path '$susPathInput'",
                                "已添加 SUS Path"
                            )
                            susPaths = susPaths + susPathInput
                            susPathInput = ""
                        },
                        enabled = !isLoading && susPathInput.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) { Text("添加") }
                    OutlinedButton(
                        onClick = {
                            execSusfs(
                                "ksu_susfs add_sus_path_loop '$susPathInput'",
                                "已添加 SUS Path (Loop)"
                            )
                            susPathInput = ""
                        },
                        enabled = !isLoading && susPathInput.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) { Text("Loop 模式") }
                }
                if (susPaths.isNotEmpty()) {
                    Text("已添加路径:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    susPaths.forEach { path ->
                        Text("• $path", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // SUS Mount 隐藏
            SuSFSSectionCard(
                icon = Icons.Filled.HideSource,
                title = "SUS Mount 隐藏",
                subtitle = "从 /proc/self/mounts 中隐藏挂载点"
            ) {
                OutlinedTextField(
                    value = susMountInput,
                    onValueChange = { susMountInput = it },
                    label = { Text("挂载点路径") },
                    placeholder = { Text("/data/adb/ksu/modules") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = {
                        execSusfs(
                            "ksu_susfs add_sus_mount '$susMountInput'",
                            "已添加 SUS Mount"
                        )
                        susMounts = susMounts + susMountInput
                        susMountInput = ""
                    },
                    enabled = !isLoading && susMountInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("添加隐藏") }
                if (susMounts.isNotEmpty()) {
                    Text("已隐藏挂载点:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    susMounts.forEach { path ->
                        Text("• $path", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Try Umount
            SuSFSSectionCard(
                icon = Icons.Filled.LinkOff,
                title = "Try Umount",
                subtitle = "对非 Root 进程卸载指定挂载路径"
            ) {
                OutlinedTextField(
                    value = tryUmountPath,
                    onValueChange = { tryUmountPath = it },
                    label = { Text("路径") },
                    placeholder = { Text("/data/adb/modules") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("模式: ", style = MaterialTheme.typography.bodyMedium)
                    FilterChip(
                        selected = tryUmountMode == "0",
                        onClick = { tryUmountMode = "0" },
                        label = { Text("无 flag") }
                    )
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = tryUmountMode == "1",
                        onClick = { tryUmountMode = "1" },
                        label = { Text("MNT_DETACH") }
                    )
                }
                Button(
                    onClick = {
                        execSusfs(
                            "ksu_susfs add_try_umount '$tryUmountPath' $tryUmountMode",
                            "已添加 Try Umount"
                        )
                        tryUmountPath = ""
                    },
                    enabled = !isLoading && tryUmountPath.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("添加") }
            }

            // SUS Map 伪造
            SuSFSSectionCard(
                icon = Icons.Filled.Map,
                title = "SUS Map 伪造",
                subtitle = "伪造 /proc/self/maps 中的库映射信息"
            ) {
                OutlinedTextField(
                    value = susMapInput,
                    onValueChange = { susMapInput = it },
                    label = { Text("库路径") },
                    placeholder = { Text("/data/adb/modules/xxx/zygisk/arm64-v8a.so") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = {
                        execSusfs(
                            "ksu_susfs add_sus_map '$susMapInput'",
                            "已添加 SUS Map"
                        )
                        susMapInput = ""
                    },
                    enabled = !isLoading && susMapInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("添加") }
            }

            // SUS Kstat 伪造
            SuSFSSectionCard(
                icon = Icons.Filled.Analytics,
                title = "SUS Kstat 伪造",
                subtitle = "伪造文件 stat 信息（ino/dev/nlink/time）"
            ) {
                OutlinedTextField(
                    value = kstatPath,
                    onValueChange = { kstatPath = it },
                    label = { Text("文件路径") },
                    placeholder = { Text("/system/bin/su") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            execSusfs(
                                "ksu_susfs add_sus_kstat '$kstatPath'",
                                "已注册 Kstat（需后续 Update）"
                            )
                        },
                        enabled = !isLoading && kstatPath.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) { Text("注册") }
                    OutlinedButton(
                        onClick = {
                            execSusfs(
                                "ksu_susfs update_sus_kstat '$kstatPath'",
                                "Kstat 已更新"
                            )
                        },
                        enabled = !isLoading && kstatPath.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) { Text("更新") }
                    OutlinedButton(
                        onClick = {
                            execSusfs(
                                "ksu_susfs update_sus_kstat_full_clone '$kstatPath'",
                                "Kstat 已完整克隆"
                            )
                        },
                        enabled = !isLoading && kstatPath.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) { Text("克隆") }
                }
            }

            // SUS Proc FD Link 伪造
            SuSFSSectionCard(
                icon = Icons.Filled.Link,
                title = "Proc FD Link 伪造",
                subtitle = "伪造 /proc/self/fd/ 符号链接目标"
            ) {
                OutlinedTextField(
                    value = susProcFdOriginal,
                    onValueChange = { susProcFdOriginal = it },
                    label = { Text("原始链接") },
                    placeholder = { Text("/dev/binder") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = susProcFdSpoofed,
                    onValueChange = { susProcFdSpoofed = it },
                    label = { Text("伪造目标") },
                    placeholder = { Text("/dev/null") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = {
                        execSusfs(
                            "ksu_susfs add_sus_proc_fd_link '$susProcFdOriginal' '$susProcFdSpoofed'",
                            "Proc FD Link 已伪造"
                        )
                        susProcFdOriginal = ""
                        susProcFdSpoofed = ""
                    },
                    enabled = !isLoading && susProcFdOriginal.isNotBlank() && susProcFdSpoofed.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("添加") }
            }

            // SUS Memfd 阻止
            SuSFSSectionCard(
                icon = Icons.Filled.Block,
                title = "SUS Memfd 阻止",
                subtitle = "阻止指定 memfd 名称创建（需前缀 memfd:）"
            ) {
                OutlinedTextField(
                    value = susMemfdInput,
                    onValueChange = { susMemfdInput = it },
                    label = { Text("Memfd 名称") },
                    placeholder = { Text("memfd:/jit-cache") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = {
                        execSusfs(
                            "ksu_susfs add_sus_memfd '$susMemfdInput'",
                            "Memfd 阻止已添加"
                        )
                        susMemfdInput = ""
                    },
                    enabled = !isLoading && susMemfdInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("添加") }
            }

            // Open Redirect
            SuSFSSectionCard(
                icon = Icons.Filled.CompareArrows,
                title = "Open Redirect",
                subtitle = "将路径访问重定向到指定路径"
            ) {
                OutlinedTextField(
                    value = openRedirectTarget,
                    onValueChange = { openRedirectTarget = it },
                    label = { Text("目标路径") },
                    placeholder = { Text("/system/bin/su") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = openRedirectDest,
                    onValueChange = { openRedirectDest = it },
                    label = { Text("重定向到") },
                    placeholder = { Text("/system/bin/sh") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text("UID 策略:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("0" to "非App进程", "1" to "Root非SU", "2" to "非SU进程", "3" to "Umounted App").forEach { (code, label) ->
                        FilterChip(
                            selected = openRedirectScheme == code,
                            onClick = { openRedirectScheme = code },
                            label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        )
                    }
                }
                Button(
                    onClick = {
                        execSusfs(
                            "ksu_susfs add_open_redirect '$openRedirectTarget' '$openRedirectDest' $openRedirectScheme",
                            "Open Redirect 已添加"
                        )
                        openRedirectTarget = ""
                        openRedirectDest = ""
                    },
                    enabled = !isLoading && openRedirectTarget.isNotBlank() && openRedirectDest.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("添加") }
            }

            // Cmdline/Bootconfig 伪造
            SuSFSSectionCard(
                icon = Icons.Filled.Terminal,
                title = "Cmdline/Bootconfig 伪造",
                subtitle = "伪造 /proc/cmdline 或 /proc/bootconfig 输出"
            ) {
                OutlinedTextField(
                    value = cmdlineInput,
                    onValueChange = { cmdlineInput = it },
                    label = { Text("伪造内容（每行一条）") },
                    placeholder = { Text("androidboot.veritymode=enforcing") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                    maxLines = 5
                )
                Button(
                    onClick = {
                        val tmpFile = "/data/local/tmp/susfs_cmdline"
                        execSusfs(
                            "echo '$cmdlineInput' > $tmpFile && ksu_susfs set_cmdline_or_bootconfig $tmpFile && rm $tmpFile",
                            "Cmdline 已伪造"
                        )
                        cmdlineInput = ""
                    },
                    enabled = !isLoading && cmdlineInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("应用") }
            }

            // 日志控制
            SuSFSSectionCard(
                icon = Icons.Filled.BugReport,
                title = "日志与高级控制",
                subtitle = "SuSFS 内核日志、挂载隐藏策略"
            ) {
                ListItem(
                    headlineContent = { Text("SuSFS 内核日志") },
                    supportingContent = { Text(if (logEnabled) "已开启" else "已关闭") },
                    trailingContent = {
                        Switch(
                            checked = logEnabled,
                            onCheckedChange = {
                                logEnabled = it
                                execSusfs("ksu_susfs enable_log ${if (it) 1 else 0}", "日志已${if (it) "开启" else "关闭"}")
                            }
                        )
                    }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("对非SU进程隐藏 SUS Mounts") },
                    supportingContent = { Text(if (hideMntsForNonSu) "已开启" else "已关闭") },
                    trailingContent = {
                        Switch(
                            checked = hideMntsForNonSu,
                            onCheckedChange = {
                                hideMntsForNonSu = it
                                execSusfs("ksu_susfs hide_sus_mnts_for_non_su_procs ${if (it) 1 else 0}", "设置已${if (it) "开启" else "关闭"}")
                            }
                        )
                    }
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SuSFSStatusCard(
    available: Boolean,
    version: String,
    features: String,
    variant: String
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (available) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (available) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                    null,
                    tint = if (available) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (available) "SuSFS 已激活" else "SuSFS 不可用",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (available) {
                Text("版本: $version", style = MaterialTheme.typography.bodyMedium)
                if (variant.isNotBlank()) {
                    Text("变体: $variant", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (features.isNotBlank()) {
                    Text("功能: $features", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SuSFSSectionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            content()
        }
    }
}
