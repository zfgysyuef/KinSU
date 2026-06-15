package com.mikokernel.ui.susfs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
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
    var sufsVersion by remember { mutableStateOf("loading...") }
    var sufsFeatures by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                sufsVersion = getSuSFSVersion()
            } catch (_: Exception) {
                sufsVersion = "unsupported"
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
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.susfs_config_title), fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.Default.Settings, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("SuSFS Status", fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Version: $sufsVersion", style = MaterialTheme.typography.bodyMedium)
                    if (sufsFeatures.isNotBlank()) {
                        Text("Features: $sufsFeatures", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Uname Spoofing", fontWeight = FontWeight.Medium)
                    Text("Customize kernel uname to hide root detection", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    OutlinedTextField(
                        value = unameValue,
                        onValueChange = { unameValue = it },
                        label = { Text("Uname Value") },
                        placeholder = { Text("Linux version x.x.x...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = buildTimeValue,
                        onValueChange = { buildTimeValue = it },
                        label = { Text("Build Time") },
                        placeholder = { Text("#1 SMP PREEMPT ...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text("Current: ${SuSFSManager.getUnameValue(context)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Build: ${SuSFSManager.getBuildTimeValue(context)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = {
                                unameValue = "default"
                                buildTimeValue = "default"
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Reset")
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
                            enabled = !isLoading,
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
