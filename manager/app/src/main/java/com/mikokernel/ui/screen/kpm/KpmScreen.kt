package com.mikokernel.ui.screen.kpm

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mikokernel.R
import com.mikokernel.data.model.KpmFileMetadata
import com.mikokernel.data.model.KpmModuleInfo
import com.mikokernel.ui.viewmodel.KpmViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val MAX_SELECTED_KPM_SIZE = 32 * 1024 * 1024

private data class PendingKpm(
    val file: File,
    val metadata: KpmFileMetadata,
)

@Composable
fun KpmScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel = viewModel<KpmViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var pendingInstall by remember { mutableStateOf<PendingKpm?>(null) }
    var pendingUninstall by remember { mutableStateOf<KpmModuleInfo?>(null) }
    var pendingControl by remember { mutableStateOf<KpmModuleInfo?>(null) }
    var controlArgs by remember { mutableStateOf("") }
    val latestPendingInstall by rememberUpdatedState(pendingInstall)

    DisposableEffect(Unit) {
        onDispose { latestPendingInstall?.file?.delete() }
    }

    val installSuccess = stringResource(R.string.kpm_install_success)
    val installFailed = stringResource(R.string.kpm_install_failed)
    val invalidFile = stringResource(R.string.kpm_invalid_file)
    val uninstallSuccess = stringResource(R.string.kpm_uninstall_success)
    val uninstallFailed = stringResource(R.string.kpm_uninstall_failed)
    val controlSuccess = stringResource(R.string.kpm_control_success)
    val controlFailed = stringResource(R.string.kpm_control_failed)

    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        scope.launch {
            val parsed = withContext(Dispatchers.IO) {
                runCatching {
                    val file = File.createTempFile("kinsu-kpm-", ".kpm", context.cacheDir)
                    try {
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            file.outputStream().use { output ->
                                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                                var total = 0
                                while (true) {
                                    val count = input.read(buffer)
                                    if (count < 0) break
                                    total += count
                                    require(total <= MAX_SELECTED_KPM_SIZE) {
                                        "KPM file is too large"
                                    }
                                    output.write(buffer, 0, count)
                                }
                            }
                        } ?: error("Unable to read selected file")

                        PendingKpm(file, inspectKpmFile(file).getOrThrow())
                    } catch (error: Throwable) {
                        file.delete()
                        throw error
                    }
                }
            }

            parsed.onSuccess { pendingInstall = it }
                .onFailure { showToast("$invalidFile: ${it.message.orEmpty()}") }
        }
    }

    pendingInstall?.let { pending ->
        AlertDialog(
            onDismissRequest = {
                pending.file.delete()
                pendingInstall = null
            },
            title = { Text(stringResource(R.string.kpm_install_title, pending.metadata.id)) },
            text = { Text(stringResource(R.string.kpm_install_mode_description)) },
            confirmButton = {
                Button(onClick = {
                    pendingInstall = null
                    scope.launch {
                        val result = try {
                            viewModel.install(pending.file, pending.metadata, persistent = true)
                        } finally {
                            pending.file.delete()
                        }
                        showToast(
                            if (result.isSuccess) installSuccess
                            else "$installFailed: ${result.exceptionOrNull()?.message.orEmpty()}"
                        )
                    }
                }) {
                    Text(stringResource(R.string.kpm_install_persistent))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingInstall = null
                    scope.launch {
                        val result = try {
                            viewModel.install(pending.file, pending.metadata, persistent = false)
                        } finally {
                            pending.file.delete()
                        }
                        showToast(
                            if (result.isSuccess) installSuccess
                            else "$installFailed: ${result.exceptionOrNull()?.message.orEmpty()}"
                        )
                    }
                }) {
                    Text(stringResource(R.string.kpm_load_once))
                }
            }
        )
    }

    pendingUninstall?.let { module ->
        AlertDialog(
            onDismissRequest = { pendingUninstall = null },
            title = { Text(stringResource(R.string.kpm_uninstall_title)) },
            text = { Text(stringResource(R.string.kpm_uninstall_confirm, module.name)) },
            confirmButton = {
                Button(onClick = {
                    pendingUninstall = null
                    scope.launch {
                        val result = viewModel.uninstall(module.id)
                        showToast(
                            if (result.isSuccess) uninstallSuccess
                            else "$uninstallFailed: ${result.exceptionOrNull()?.message.orEmpty()}"
                        )
                    }
                }) {
                    Text(stringResource(R.string.uninstall))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingUninstall = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    pendingControl?.let { module ->
        AlertDialog(
            onDismissRequest = { pendingControl = null },
            title = { Text(stringResource(R.string.kpm_control_title, module.name)) },
            text = {
                OutlinedTextField(
                    value = controlArgs,
                    onValueChange = { controlArgs = it },
                    label = { Text(stringResource(R.string.kpm_control_args)) },
                    singleLine = true,
                )
            },
            confirmButton = {
                Button(onClick = {
                    pendingControl = null
                    val submittedArgs = controlArgs
                    scope.launch {
                        val result = viewModel.control(module.id, submittedArgs)
                        showToast(
                            if (result.getOrNull() == 0) controlSuccess
                            else "$controlFailed: ${result.exceptionOrNull()?.message ?: result.getOrNull()}"
                        )
                    }
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingControl = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    KpmMaterial(
        state = state,
        onRefresh = viewModel::refresh,
        onInstall = { picker.launch(arrayOf("application/octet-stream", "*/*")) },
        onUninstall = { pendingUninstall = it },
        onControl = {
            controlArgs = it.args
            pendingControl = it
        },
    )
}
