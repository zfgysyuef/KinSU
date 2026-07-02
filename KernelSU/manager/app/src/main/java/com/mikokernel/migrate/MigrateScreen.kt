package com.mikokernel.migrate

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mikokernel.R
import com.mikokernel.ui.navigation3.Navigator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MigrateScreen(
    navigator: Navigator,
    viewModel: MigrateViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("管理器迁移") },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedContent(
                targetState = uiState.step,
                label = "migrate_step"
            ) { step ->
                when (step) {
                    MigrateStep.SELECT_ACTION -> SelectActionContent(
                        onDetectConflicts = { viewModel.detectConflicts() },
                        onMigrate = { viewModel.goToSelectSource() }
                    )
                    MigrateStep.DETECTING -> LoadingContent("正在扫描冲突...")
                    MigrateStep.CONFLICTS_FOUND -> ConflictsFoundContent(
                        conflicts = uiState.conflicts,
                        onClean = { viewModel.cleanConflicts(backup = true) },
                        onSkipClean = { viewModel.goToSelectSource() },
                        onBack = { viewModel.goToSelectAction() }
                    )
                    MigrateStep.CLEANING -> LoadingContent("正在清理冲突...")
                    MigrateStep.CLEAN_RESULT -> CleanResultContent(
                        result = uiState.cleanResult,
                        onContinue = { viewModel.goToSelectSource() },
                        onBack = { viewModel.goToSelectAction() }
                    )
                    MigrateStep.SELECT_SOURCE -> SelectSourceContent(
                        selectedSource = uiState.selectedSource,
                        preserveData = uiState.preserveData,
                        onSelectSource = { viewModel.selectSource(it) },
                        onSetPreserveData = { viewModel.setPreserveData(it) },
                        onStartMigration = { viewModel.startMigration() },
                        onBack = { viewModel.goToSelectAction() }
                    )
                    MigrateStep.MIGRATING -> LoadingContent("正在迁移模块...")
                    MigrateStep.MIGRATE_RESULT -> MigrateResultContent(
                        result = uiState.migrateResult,
                        onDone = { viewModel.goToSelectAction() }
                    )
                }
            }

            // Error snackbar
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("确定")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
private fun SelectActionContent(
    onDetectConflicts: () -> Unit,
    onMigrate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "从其他管理器切换到 KinSU 时，可能存在残留文件冲突。",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedCard(
            onClick = onDetectConflicts,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("检测冲突", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "扫描 Magisk、APatch、旧版 KernelSU 的残留文件",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OutlinedCard(
            onClick = onMigrate,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("迁移模块", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "将其他管理器的模块迁移到 KinSU",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LoadingContent(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ConflictsFoundContent(
    conflicts: List<ConflictItem>,
    onClean: () -> Unit,
    onSkipClean: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            "检测到 ${conflicts.size} 个冲突",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(conflicts) { conflict ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (conflict.type) {
                                "modules" -> Icons.Default.Extension
                                "init" -> Icons.Default.Code
                                "sepolicy" -> Icons.Default.Security
                                else -> Icons.Default.Warning
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                conflict.path,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "${conflict.source} · ${conflict.type}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("返回")
            }
            OutlinedButton(onClick = onSkipClean, modifier = Modifier.weight(1f)) {
                Text("跳过清理")
            }
            Button(onClick = onClean, modifier = Modifier.weight(1f)) {
                Text("清理冲突")
            }
        }
    }
}

@Composable
private fun CleanResultContent(
    result: CleanResult?,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        result?.let { r ->
            Icon(
                imageVector = if (r.success) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (r.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                if (r.success) "清理完成" else "部分清理失败",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("已清理: ${r.cleanedCount}，失败: ${r.failedCount}")

            r.backupPath?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text("备份位置: $it", style = MaterialTheme.typography.bodySmall)
            }

            if (r.details.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(r.details) { detail ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (detail.success) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (detail.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(detail.path, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("返回")
            }
            Button(onClick = onContinue, modifier = Modifier.weight(1f)) {
                Text("继续迁移")
            }
        }
    }
}

@Composable
private fun SelectSourceContent(
    selectedSource: MigrationSource?,
    preserveData: Boolean,
    onSelectSource: (MigrationSource) -> Unit,
    onSetPreserveData: (Boolean) -> Unit,
    onStartMigration: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            "选择迁移来源",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        MigrationSource.entries.forEach { source ->
            Card(
                onClick = { onSelectSource(source) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedSource == source)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedSource == source,
                        onClick = { onSelectSource(source) }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(source.displayName, style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = preserveData,
                onCheckedChange = onSetPreserveData
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("保留模块数据")
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("返回")
            }
            Button(
                onClick = onStartMigration,
                enabled = selectedSource != null,
                modifier = Modifier.weight(1f)
            ) {
                Text("开始迁移")
            }
        }
    }
}

@Composable
private fun MigrateResultContent(
    result: MigrateResult?,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        result?.let { r ->
            Icon(
                imageVector = if (r.success) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (r.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                if (r.success) "迁移完成" else "迁移部分完成",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("来源: ${r.source}")
            Text("检测: ${r.detectedCount}，迁移: ${r.migratedCount}，跳过: ${r.skippedCount}，失败: ${r.failedCount}")

            if (r.details.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("详细信息", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(r.details) { detail ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (detail.status) {
                                            "migrated" -> Icons.Default.CheckCircle
                                            "skipped" -> Icons.Default.SkipNext
                                            else -> Icons.Default.Error
                                        },
                                        contentDescription = null,
                                        tint = when (detail.status) {
                                            "migrated" -> MaterialTheme.colorScheme.primary
                                            "skipped" -> MaterialTheme.colorScheme.outline
                                            else -> MaterialTheme.colorScheme.error
                                        },
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(detail.moduleId, fontWeight = FontWeight.Medium)
                                }
                                detail.reason?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            r.cleanResult?.let { clean ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "冲突清理: 清理 ${clean.cleanedCount}，失败 ${clean.failedCount}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("完成")
        }
    }
}
