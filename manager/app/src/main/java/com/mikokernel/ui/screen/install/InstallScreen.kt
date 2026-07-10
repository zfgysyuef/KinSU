package com.mikokernel.ui.screen.install

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.dropUnlessResumed
import kotlinx.coroutines.launch
import com.mikokernel.R
import com.mikokernel.getKernelVersion
import com.mikokernel.isGkiDevice
import com.mikokernel.ui.component.choosekmidialog.ChooseKmiDialog
import com.mikokernel.ui.navigation3.LocalNavigator
import com.mikokernel.ui.navigation3.Route
import com.mikokernel.ui.screen.flash.FlashIt
import com.mikokernel.ui.util.LkmSelection
import com.mikokernel.ui.util.getAvailablePartitions
import com.mikokernel.ui.util.getCurrentKmi
import com.mikokernel.ui.util.getDefaultPartition
import com.mikokernel.ui.util.getSlotSuffix
import com.mikokernel.ui.util.isAbDevice
import com.mikokernel.ui.util.rootAvailable
import com.mikokernel.ui.util.ZipFileDetector

@Composable
fun InstallScreen() {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val resources = LocalResources.current

    var installMethod by rememberSaveable { mutableStateOf<InstallMethod?>(null) }
    var lkmSelection by rememberSaveable { mutableStateOf<LkmSelection>(LkmSelection.KmiNone) }
    var partitionSelectionIndex by rememberSaveable { mutableIntStateOf(0) }
    var hasCustomSelected by rememberSaveable { mutableStateOf(false) }
    val showChooseKmiDialog = rememberSaveable { mutableStateOf(false) }
    var advancedOptionsShown by rememberSaveable { mutableStateOf(false) }
    var allowShell by rememberSaveable { mutableStateOf(false) }
    var enableAdb by rememberSaveable { mutableStateOf(false) }
    var patchEnableKpm by rememberSaveable { mutableStateOf(false) }
    var patchEnableSusfs by rememberSaveable { mutableStateOf(false) }

    val currentKmi by produceState(initialValue = "") { value = getCurrentKmi() }
    val partitions by produceState(initialValue = emptyList()) { value = getAvailablePartitions() }
    val defaultPartition by produceState(initialValue = "") { value = getDefaultPartition() }
    val rootAvailable by produceState(initialValue = false) { value = rootAvailable() }
    val isAbDevice by produceState(initialValue = false) { value = isAbDevice() }
    val isGkiDevice by produceState(initialValue = false) { value = isGkiDevice() }

    val selectFileTip = stringResource(id = R.string.select_file_tip, defaultPartition)
    val selectFileTipNoGki = stringResource(id = R.string.select_file_tip_nogki)
    val installMethodOptions = remember(rootAvailable, isAbDevice, isGkiDevice, selectFileTip, selectFileTipNoGki) {
        buildList {
            add(InstallMethod.SelectFile(summary = if (isGkiDevice) selectFileTip else selectFileTipNoGki))
            if (rootAvailable) {
                add(InstallMethod.DirectInstall)
                if (isAbDevice) add(InstallMethod.DirectInstallToInactiveSlot)
            }
            add(InstallMethod.HorizonKernel())
        }
    }

    val isOta = installMethod is InstallMethod.DirectInstallToInactiveSlot
    val slotSuffix by produceState(initialValue = "", isOta) { value = getSlotSuffix(isOta) }
    val defaultIndex = remember(partitions, defaultPartition) {
        partitions.indexOf(defaultPartition).coerceAtLeast(0)
    }

    LaunchedEffect(partitions, defaultIndex, hasCustomSelected) {
        if (partitions.isEmpty()) return@LaunchedEffect
        if (!hasCustomSelected) {
            partitionSelectionIndex = defaultIndex.coerceIn(0, partitions.lastIndex)
        } else if (partitionSelectionIndex > partitions.lastIndex) {
            partitionSelectionIndex = partitions.lastIndex
        }
    }

    val displayPartitions = remember(partitions, defaultPartition) {
        partitions.map { name -> if (defaultPartition == name) "$name (default)" else name }
    }

    fun showMessage(message: String) {
        scope.launch {
            snackbarHost.showSnackbar(message)
        }
    }

    val onInstall = {
        installMethod?.let { method ->
            when (method) {
                is InstallMethod.PatchKernel -> {
                    val bootUri = if (!rootAvailable) {
                        (installMethod as? InstallMethod.SelectFile)?.uri
                    } else {
                        null
                    }
                    navigator.push(
                        Route.Flash(
                            FlashIt.FlashBoot(
                                boot = bootUri,
                                lkm = lkmSelection,
                                ota = false,
                                partition = partitions.getOrNull(partitionSelectionIndex),
                                allowShell = allowShell,
                                enableAdb = enableAdb,
                                enableKpm = patchEnableKpm,
                                enableSusfs = patchEnableSusfs,
                            )
                        )
                    )
                }
                is InstallMethod.HorizonKernel -> {
                    navigator.push(
                        Route.AnyKernel3Flash(
                            kernelUri = method.uri.toString(),
                            slot = method.slot
                        )
                    )
                }
                else -> {
                    navigator.push(
                        Route.Flash(
                            FlashIt.FlashBoot(
                                boot = if (method is InstallMethod.SelectFile) method.uri else null,
                                lkm = lkmSelection,
                                ota = method is InstallMethod.DirectInstallToInactiveSlot,
                                partition = partitions.getOrNull(partitionSelectionIndex),
                                allowShell = allowShell,
                                enableAdb = enableAdb,
                                enableKpm = patchEnableKpm,
                                enableSusfs = patchEnableSusfs,
                            )
                        )
                    )
                }
            }
        }
    }

    ChooseKmiDialog(
        show = showChooseKmiDialog.value,
        onDismissRequest = { showChooseKmiDialog.value = false },
        onSelected = { kmi ->
            kmi?.let {
                lkmSelection = LkmSelection.KmiString(it)
                onInstall()
            }
        }
    )

    val selectLkmLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.data?.let { uri ->
                if (isKoFile(context, uri)) {
                    lkmSelection = LkmSelection.LkmUri(uri)
                } else {
                    lkmSelection = LkmSelection.KmiNone
                    showMessage(resources.getString(R.string.install_only_support_ko_file))
                }
            }
        }
    }
    val selectImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.data?.let { uri ->
                installMethod = InstallMethod.SelectFile(uri, summary = if (isGkiDevice) selectFileTip else selectFileTipNoGki)
            }
        }
    }
    val selectAnyKernel3Launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.data?.let { uri ->
                val type = ZipFileDetector.detectZipType(context, uri)
                if (type == com.mikokernel.ui.util.ZipType.KERNEL) {
                    installMethod = InstallMethod.HorizonKernel(uri = uri)
                    onInstall()
                } else {
                    showMessage(if (type == com.mikokernel.ui.util.ZipType.MODULE) "This is a module, not a kernel ZIP" else "Not a valid AnyKernel3 kernel ZIP")
                }
            }
        }
    }

    val state = InstallUiState(
        installMethod = installMethod,
        lkmSelection = lkmSelection,
        partitionSelectionIndex = partitionSelectionIndex,
        displayPartitions = displayPartitions,
        currentKmi = currentKmi,
        slotSuffix = slotSuffix,
        installMethodOptions = installMethodOptions,
        canSelectPartition = installMethod is InstallMethod.DirectInstall || installMethod is InstallMethod.DirectInstallToInactiveSlot,
        advancedOptionsShown = advancedOptionsShown,
        allowShell = allowShell,
        enableAdb = enableAdb,
        patchEnableKpm = patchEnableKpm,
        patchEnableSusfs = patchEnableSusfs,
    )
    val actions = InstallScreenActions(
        onBack = dropUnlessResumed { navigator.pop() },
        onSelectMethod = { method -> installMethod = method },
        onSelectBootImage = {
            selectImageLauncher.launch(Intent(Intent.ACTION_GET_CONTENT).apply { type = "application/octet-stream" })
        },
        onSelectHorizonKernel = {
            selectAnyKernel3Launcher.launch(Intent(Intent.ACTION_GET_CONTENT).apply { type = "application/zip" })
        },
        onUploadLkm = {
            selectLkmLauncher.launch(Intent(Intent.ACTION_GET_CONTENT).apply { type = "application/octet-stream" })
        },
        onClearLkm = { lkmSelection = LkmSelection.KmiNone },
        onSelectPartition = { index ->
            hasCustomSelected = true
            partitionSelectionIndex = index
        },
        onNext = {
            val isLkmSelected = lkmSelection != LkmSelection.KmiNone
            val isKmiUnknown = currentKmi.isBlank()
            val isSelectFileMode = installMethod is InstallMethod.SelectFile
            val isHorizonKernelNoUri = installMethod is InstallMethod.HorizonKernel && (installMethod as InstallMethod.HorizonKernel).uri == null
            if (isHorizonKernelNoUri) {
                // AnyKernel3 selected from radio but no ZIP chosen yet - open file picker
                selectAnyKernel3Launcher.launch(Intent(Intent.ACTION_GET_CONTENT).apply { type = "application/zip" })
            } else if (!patchEnableKpm && !isLkmSelected && (isKmiUnknown || isSelectFileMode)) {
                showChooseKmiDialog.value = true
            } else {
                onInstall()
            }
        },
        onAdvancedOptionsClicked = {
            advancedOptionsShown = !advancedOptionsShown
        },
        onSelectAllowShell = {
            allowShell = it
        },
        onSelectEnableAdb = {
            enableAdb = it
        },
        onTogglePatchKpm = {
            patchEnableKpm = it
        },
        onTogglePatchSusfs = {
            patchEnableSusfs = it
        },
    )

    InstallScreenMaterial(state, actions, snackbarHost)
}
