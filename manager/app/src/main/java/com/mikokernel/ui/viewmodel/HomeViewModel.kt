package com.mikokernel.ui.viewmodel

import android.content.Context
import android.os.Build
import android.system.Os
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.mikokernel.BuildConfig
import com.mikokernel.Natives
import com.mikokernel.getKernelVersion
import com.mikokernel.isGkiDevice
import com.mikokernel.ksuApp
import com.mikokernel.ui.screen.home.HomeUiState
import com.mikokernel.ui.screen.home.SystemInfo
import com.mikokernel.ui.screen.home.getManagerVersion
import com.mikokernel.ui.util.checkNewVersion
import com.mikokernel.ui.util.getModuleCount
import com.mikokernel.ui.util.getSELinuxStatusRaw
import com.mikokernel.ui.util.getSuperuserCount
import com.mikokernel.ui.util.getRootShell
import com.mikokernel.ui.util.kpmGetVersion
import com.mikokernel.ui.util.module.LatestVersionInfo
import com.mikokernel.ui.util.resolveDeviceName
import com.mikokernel.ui.util.rootAvailable
import com.topjohnwu.superuser.ShellUtils

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(buildState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val baseState = withContext(Dispatchers.IO) { buildState() }
            _uiState.update { baseState }
            if (baseState.checkUpdateEnabled) {
                val latestVersionInfo = withContext(Dispatchers.IO) { checkNewVersion() }
                _uiState.update { it.copy(latestVersionInfo = latestVersionInfo) }
            }
        }
    }

    private fun buildState(): HomeUiState {
        val prefs = ksuApp.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val kernelVersion = getKernelVersion()
        val isManager = Natives.isManager
        val ksuVersion = if (isManager) Natives.version else null
        val kernelUAPIVersion = if (isManager) Natives.kernelUAPIVersion else null
        val managerUAPIVersion = Natives.managerUAPIVersion
        val lkmMode = ksuVersion?.let { if (isGkiDevice()) Natives.isLkmMode else null }
        // Probe the real KernelPatch VERSION operation. No hard-coded or persisted
        // manager flag is accepted as proof that the early-boot bridge is active.
        val isKpmActive = if (ksuVersion != null) {
            try {
                val version = kpmGetVersion()
                version.isNotBlank() && version != "unsupported" && !version.startsWith("N/A")
            } catch (_: Exception) {
                false
            }
        } else {
            false
        }
        val isRootAvailable = rootAvailable()
        val managerVersion = getManagerVersion(ksuApp)

        // Detect SuSFS integration: only check on GKI devices with kernel active
        val isSusfsAvailable = if (ksuVersion != null && isGkiDevice()) {
            try {
                val shell = getRootShell()
                val ver = ShellUtils.fastCmd(shell, "ksu_susfs show version 2>/dev/null").trim()
                ver.isNotBlank() && ver != "unsupport"
            } catch (_: Exception) {
                false
            }
        } else {
            false
        }

        return HomeUiState(
            kernelVersion = kernelVersion,
            ksuVersion = ksuVersion,
            lkmMode = lkmMode,
            isKpmActive = isKpmActive,
            isManager = isManager,
            isManagerPrBuild = BuildConfig.IS_PR_BUILD,
            isKernelPrBuild = Natives.isPrBuild,
            requiresNewKernel = isManager && Natives.requireNewKernel(),
            uapiMismatch = isManager && Natives.checkUAPIMismatch(),
            kernelUAPIVersion = kernelUAPIVersion,
            managerUAPIVersion = managerUAPIVersion,
            isRootAvailable = isRootAvailable,
            isSafeMode = Natives.isSafeMode,
            isLateLoadMode = Natives.isLateLoadMode,
            isSusfsAvailable = isSusfsAvailable,
            checkUpdateEnabled = prefs.getBoolean("check_update", true),
            latestVersionInfo = LatestVersionInfo(),
            currentManagerVersionCode = managerVersion.versionCode,
            superuserCount = getSuperuserCount(),
            moduleCount = getModuleCount(),
            systemInfo = SystemInfo(
                kernelVersion = Os.uname().release,
                managerVersion = "${managerVersion.versionName} (${managerVersion.versionCode}/${managerUAPIVersion})",
                deviceModel = resolveDeviceName(),
                fingerprint = Build.FINGERPRINT,
                selinuxStatus = getSELinuxStatusRaw(),
                seccompStatus = runCatching {
                    Os.prctl(21 /* PR_GET_SECCOMP */, 0, 0, 0, 0)
                }.getOrDefault(-1),
            ),
        )
    }
}
