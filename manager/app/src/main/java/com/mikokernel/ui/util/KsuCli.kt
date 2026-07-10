/*
 * KinSU - A derivative work of KernelSU
 * Copyright (c) 2022-2024 weishu (KernelSU Project)
 * Copyright (c) 2024 KinSU Project
 *
 * Licensed under GPLv3. See NOTICE at project root for full attribution.
 * Original source: https://github.com/tiann/KernelSU
 * Original author: weishu
 */

package com.mikokernel.ui.util

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.os.Parcelable
import android.os.SystemClock
import android.provider.OpenableColumns
import android.system.Os
import android.util.Log
import com.topjohnwu.superuser.CallbackList
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ShellUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import com.mikokernel.BuildConfig
import com.mikokernel.Natives
import com.mikokernel.ksuApp
import org.json.JSONArray
import java.io.File

/**
 * @author weishu
 * @date 2023/1/1.
 */
private const val TAG = "KsuCli"

/**
 * 返回 KinSU daemon 路径 (libkinsud.so)。
 * 始终不变 — root 授权和常规模块管理使用此 daemon。
 * 注意: 文件名必须与 repack_apk.py 中的 libkinsud.so 一致
 */
private fun getKsuDaemonPath(): String {
    return ksuApp.applicationInfo.nativeLibraryDir + File.separator + "libkinsud.so"
}


data class FlashResult(val code: Int, val err: String, val showReboot: Boolean) {
    constructor(result: Shell.Result, showReboot: Boolean) : this(result.code, result.err.joinToString("\n"), showReboot)
    constructor(result: Shell.Result) : this(result, result.isSuccess)
}

object KsuCli {
    val SHELL: Shell = createRootShell()
    val GLOBAL_MNT_SHELL: Shell = createRootShell(true)
}

fun getRootShell(globalMnt: Boolean = false): Shell {
    return if (globalMnt) KsuCli.GLOBAL_MNT_SHELL else {
        KsuCli.SHELL
    }
}

inline fun <T> withNewRootShell(
    globalMnt: Boolean = false,
    block: Shell.() -> T
): T {
    return createRootShell(globalMnt).use(block)
}

fun Uri.getFileName(context: Context): String? {
    var fileName: String? = null
    val contentResolver: ContentResolver = context.contentResolver
    val cursor: Cursor? = contentResolver.query(this, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            fileName = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
        }
    }
    return fileName
}

fun createRootShell(globalMnt: Boolean = false): Shell {
    Shell.enableVerboseLogging = BuildConfig.DEBUG
    val builder = Shell.Builder.create()

    // Root 始终通过 libkinsud.so (KernelSU ioctl) 获取
    // KPM 模式只影响内核修补和 KPM 模块管理，不影响 root 授权
    val daemonPath = getKsuDaemonPath()
    val daemonArgs = if (globalMnt) listOf("debug", "su", "-g") else listOf("debug", "su")

    return try {
        builder.build(daemonPath, *daemonArgs.toTypedArray())
    } catch (e: Throwable) {
        Log.w(TAG, "daemon ($daemonPath) failed: ", e)
        try {
            if (globalMnt) {
                builder.build("su", "-mm")
            } else {
                builder.build("su")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "su failed: ", e)
            builder.build("sh")
        }
    }
}

fun execKsud(args: String, newShell: Boolean = false): Boolean {
    return if (newShell) {
        withNewRootShell {
            ShellUtils.fastCmdResult(this, "${getKsuDaemonPath()} $args")
        }
    } else {
        ShellUtils.fastCmdResult(getRootShell(), "${getKsuDaemonPath()} $args")
    }
}

suspend fun getFeatureStatus(feature: String): String = withContext(Dispatchers.IO) {
    val shell = getRootShell()
    val escaped = feature.replace("'", "'\\''")
    val out = shell.newJob()
        .add("${getKsuDaemonPath()} feature check '$escaped'").to(ArrayList<String>(), null).exec().out
    out.firstOrNull()?.trim().orEmpty()
}

suspend fun getFeaturePersistValue(feature: String): Long? = withContext(Dispatchers.IO) {
    val shell = getRootShell()
    val escaped = feature.replace("'", "'\\''")
    val out = shell.newJob()
        .add("${getKsuDaemonPath()} feature get --config '$escaped'").to(ArrayList<String>(), null).exec().out
    val valueLine = out.firstOrNull { it.trim().startsWith("Value:") } ?: return@withContext null
    valueLine.substringAfter("Value:").trim().toLongOrNull()
}

fun install() {
    val start = SystemClock.elapsedRealtime()
    val libadbroot = File(ksuApp.applicationInfo.nativeLibraryDir, "libadbroot.so").absolutePath
    val result = execKsud("install --libadbroot $libadbroot", true)
    Log.w(TAG, "install result: $result, cost: ${SystemClock.elapsedRealtime() - start}ms")
}

fun listModules(): String {
    val shell = getRootShell()

    val out = shell.newJob()
        .add("${getKsuDaemonPath()} module list").to(ArrayList(), null).exec().out
    return out.joinToString("\n").ifBlank { "[]" }
}

fun getModuleCount(): Int {
    val result = listModules()
    runCatching {
        val array = JSONArray(result)
        return array.length()
    }.getOrElse { return 0 }
}

fun getSuperuserCount(): Int {
    return Natives.getSuperuserCount()
}

fun toggleModule(id: String, enable: Boolean): Boolean {
    val escaped = id.replace("'", "'\\''")
    val cmd = if (enable) {
        "module enable '$escaped'"
    } else {
        "module disable '$escaped'"
    }
    val result = execKsud(cmd, true)
    Log.i(TAG, "$cmd result: $result")
    return result
}

fun undoUninstallModule(id: String): Boolean {
    val escaped = id.replace("'", "'\\''")
    val cmd = "module undo-uninstall '$escaped'"
    val result = execKsud(cmd, true)
    Log.i(TAG, "undo uninstall module $id result: $result")
    return result
}

fun uninstallModule(id: String): Boolean {
    val escaped = id.replace("'", "'\\''")
    val cmd = "module uninstall '$escaped'"
    val result = execKsud(cmd, true)
    Log.i(TAG, "uninstall module $id result: $result")
    return result
}

private fun flashWithIO(
    cmd: String,
    onStdout: (String) -> Unit,
    onStderr: (String) -> Unit
): Shell.Result {

    val stdoutCallback: CallbackList<String?> = object : CallbackList<String?>() {
        override fun onAddElement(s: String?) {
            onStdout(s ?: "")
        }
    }

    val stderrCallback: CallbackList<String?> = object : CallbackList<String?>() {
        override fun onAddElement(s: String?) {
            onStderr(s ?: "")
        }
    }

    return withNewRootShell {
        newJob().add(cmd).to(stdoutCallback, stderrCallback).exec()
    }
}

fun flashModule(
    uri: Uri,
    onStdout: (String) -> Unit,
    onStderr: (String) -> Unit
): FlashResult {
    val resolver = ksuApp.contentResolver
    resolver.openInputStream(uri)?.use { input ->
        val suffix = System.currentTimeMillis()
        val cacheFile = File(ksuApp.cacheDir, "module_$suffix.zip")
        cacheFile.outputStream().use { output ->
            input.copyTo(output)
        }

        // Read official installer.sh from APK raw resource (guaranteed LF line endings)
        val installerScript = ksuApp.resources.openRawResource(com.mikokernel.R.raw.installer)
            .bufferedReader().use { it.readText() }

        val verCode = BuildConfig.VERSION_CODE
        val verName = BuildConfig.VERSION_NAME

        // Prepend env exports, then installer.sh, then override MAGISK_VER again
        // (installer.sh hardcodes MAGISK_VER=25.2 at line 478, must override AFTER)
        // 注意：必须显式使用 "\n" 作为行尾，不能用 appendLine() ——
        // Windows JVM 上 appendLine() 会写入 \r\n，导致 sh 解析失败
        // （umask illegal mode、for...do syntax error、空行 : not found）
        val scriptWithEnv = buildString {
            append("export ASH_STANDALONE=1\n")
            append("export KSU=true\n")
            append("export KSU_KERNEL_VER_CODE=\$(cat /proc/version 2>/dev/null | head -1 || echo unknown)\n")
            append("export KSU_VER_CODE=$verCode\n")
            append("export KSU_VER=$verName\n")
            append("export PATH=/system/bin:/data/adb/ksu/bin:\$PATH\n")
            append("export OUTFD=1\n")
            append("export ZIPFILE=/data/local/tmp/kmodule_$suffix.zip\n")
            append("\n")
            append(installerScript)
            append("\n")
            // Override MAGISK_VER after installer.sh (it hardcodes 25.2)
            append("export MAGISK_VER=KinSu\n")
            append("export MAGISK_VER_CODE=$verCode\n")
            append("install_module\n")
            append("rm -f /data/local/tmp/install_$suffix.sh /data/local/tmp/kmodule_$suffix.zip\n")
            append("exit 0\n")
        }
        val installSh = File(ksuApp.cacheDir, "install_$suffix.sh")
        // 兜底：清除任何残留的 \r（包括 raw installer.sh 自身可能携带的）
        installSh.writeText(scriptWithEnv.replace("\r\n", "\n").replace("\r", "\n"), Charsets.UTF_8)

        val cmd = "cat ${cacheFile.absolutePath} > /data/local/tmp/kmodule_$suffix.zip && " +
            "cat ${installSh.absolutePath} > /data/local/tmp/install_$suffix.sh && " +
            "if [ -x /data/adb/ksu/bin/busybox ]; then /data/adb/ksu/bin/busybox sh /data/local/tmp/install_$suffix.sh; else sh /data/local/tmp/install_$suffix.sh; fi"

        val result = flashWithIO(cmd, onStdout, onStderr)
        Log.i("KinSU", "install module $uri result: $result")
        cacheFile.delete()
        installSh.delete()
        return FlashResult(result)
    } ?: return FlashResult(1, "Failed to open module file", false)
}

fun runModuleAction(
    moduleId: String, onStdout: (String) -> Unit, onStderr: (String) -> Unit
): Boolean {
    val stdoutCallback: CallbackList<String?> = object : CallbackList<String?>() {
        override fun onAddElement(s: String?) {
            onStdout(s ?: "")
        }
    }

    val stderrCallback: CallbackList<String?> = object : CallbackList<String?>() {
        override fun onAddElement(s: String?) {
            onStderr(s ?: "")
        }
    }

    val escapedId = moduleId.replace("'", "'\\''")
    val result = withNewRootShell(true) {
        newJob().add("${getKsuDaemonPath()} module action '$escapedId'")
            .to(stdoutCallback, stderrCallback).exec()
    }

    Log.i("KinSU", "Module runAction result: $result")

    return result.isSuccess
}

fun restoreBoot(
    onStdout: (String) -> Unit, onStderr: (String) -> Unit
): FlashResult {
    val result = flashWithIO("${getKsuDaemonPath()} boot-restore -f", onStdout, onStderr)
    return FlashResult(result)
}

fun uninstallPermanently(
    onStdout: (String) -> Unit, onStderr: (String) -> Unit
): FlashResult {
    val result = flashWithIO("${getKsuDaemonPath()} uninstall --package-name ${BuildConfig.APPLICATION_ID}", onStdout, onStderr)
    return FlashResult(result)
}

@Parcelize
sealed class LkmSelection : Parcelable {
    @Parcelize
    data class LkmUri(val uri: Uri) : LkmSelection()

    @Parcelize
    data class KmiString(val value: String) : LkmSelection()

    @Parcelize
    data object KmiNone : LkmSelection()
}

fun resolveKmiFromKernel(): String? {
    val rel = Os.uname().release
    return when {
        rel.contains("android16") -> when {
            rel.contains("6.12") -> "android16-6.12"
            rel.contains("6.6") -> "android16-6.6"
            else -> "android16-6.12"
        }
        rel.contains("android15") -> when {
            rel.contains("6.6") -> "android15-6.6"
            rel.contains("5.15") -> "android15-5.15"
            else -> "android15-6.6"
        }
        rel.contains("android14") -> when {
            rel.contains("6.1") -> "android14-6.1"
            rel.contains("5.15") -> "android14-5.15"
            else -> "android14-6.1"
        }
        rel.contains("android13") -> when {
            rel.contains("5.15") -> "android13-5.15"
            rel.contains("5.10") -> "android13-5.10"
            else -> "android13-5.10"
        }
        rel.contains("android12") -> "android12-5.10"
        else -> null
    }
}

fun installBoot(
    bootUri: Uri?,
    lkm: LkmSelection,
    ota: Boolean,
    partition: String?,
    allowShell: Boolean,
    enableAdb: Boolean,
    enableKpm: Boolean,
    enableSusfs: Boolean,
    onStdout: (String) -> Unit,
    onStderr: (String) -> Unit,
): FlashResult {
    val resolver = ksuApp.contentResolver

    val bootFile = bootUri?.let { uri ->
        with(resolver.openInputStream(uri)) {
            // 根据传入文件名判断是 boot 还是 init_boot，避免临时文件命名混淆
            // 用户传入 init_boot.img 时，临时文件应命名为 init_boot.img 而非 boot.img
            val originName = uri.getFileName(ksuApp).orEmpty()
            val tempName = if (originName.contains("init_boot", ignoreCase = true)) {
                "init_boot.img"
            } else {
                "boot.img"
            }
            val bootFile = File(ksuApp.cacheDir, tempName)
            bootFile.outputStream().use { output ->
                this?.copyTo(output)
            }

            bootFile
        }
    }

    var kptoolsFile: File? = null
    var kpimgFile: File? = null
    var cmd = "boot-patch"

    cmd += if (bootFile == null) {
        // no boot.img, use -f to flash
        " -f"
    } else {
        " -b ${bootFile.absolutePath}"
    }

    if (allowShell) {
        cmd += " --allow-shell"
    }

    if (enableAdb) {
        cmd += " --enable-adbd"
    }

    if (enableKpm) {
        fun copyKpmAsset(name: String): File {
            val file = File(ksuApp.cacheDir, "kinsu-$name")
            ksuApp.assets.open("kpm/$name").use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            return file
        }

        val extractedKptools = copyKpmAsset("kptools")
        val extractedKpimg = copyKpmAsset("kpimg")
        kptoolsFile = extractedKptools
        kpimgFile = extractedKpimg
        cmd += " --enable-kpm"
        cmd += " --kptools '${extractedKptools.absolutePath.replace("'", "'\\''")}'"
        cmd += " --kpimg '${extractedKpimg.absolutePath.replace("'", "'\\''")}'"
    }

    if (enableSusfs) {
        cmd += " --enable-susfs"
    }

    if (ota) {
        cmd += " -u"
    }

    var lkmFile: File? = null
    if (!enableKpm) {
        when (lkm) {
            is LkmSelection.LkmUri -> {
                lkmFile = with(resolver.openInputStream(lkm.uri)) {
                    val file = File(ksuApp.cacheDir, "kernelsu-tmp-lkm.ko")
                    file.outputStream().use { output ->
                        this?.copyTo(output)
                    }

                    file
                }
                cmd += " -m ${lkmFile.absolutePath}"
            }

            is LkmSelection.KmiString -> {
                val escapedKmi = lkm.value.replace("'", "'\\''")
                cmd += " --kmi '$escapedKmi'"
            }

            LkmSelection.KmiNone -> {
                val kmi = resolveKmiFromKernel()
                if (kmi != null) {
                    cmd += " --kmi $kmi"
                }
            }
        }
    }

    var susfsFile: File? = null
    if (enableSusfs) {
        val susfsAsset = listOf("ksu_susfs_2.1.0", "ksu_susfs_2.0.0").firstOrNull { asset ->
            try {
                ksuApp.assets.open(asset).close()
                true
            } catch (_: Exception) {
                false
            }
        }
        if (susfsAsset != null) {
            susfsFile = File(ksuApp.cacheDir, "ksu_susfs").also { file ->
                ksuApp.assets.open(susfsAsset).use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
            }
            cmd += " --susfs-binary ${susfsFile.absolutePath}"
        }
    }

    // output dir
    if (bootFile != null) {
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        cmd += " -o $downloadsDir"
    }

    partition?.let { part ->
        val escapedPart = part.replace("'", "'\\''")
        cmd += " --partition '$escapedPart'"
    }

    val result = flashWithIO("${getKsuDaemonPath()} $cmd", onStdout, onStderr)
    Log.i("KinSU", "install boot result: ${result.isSuccess}")

    bootFile?.delete()
    lkmFile?.delete()
    susfsFile?.delete()
    kptoolsFile?.delete()
    kpimgFile?.delete()

    // if boot uri is empty, it is direct install, when success, we should show reboot button
    val showReboot = bootUri == null && result.isSuccess // we create a temporary val here, to avoid calc showReboot double
    if (showReboot) { // because we decide do not update ksud when startActivity
        install() // install ksud here
    }
    return FlashResult(result, showReboot)
}

fun reboot(reason: String = "") {
    if (reason == "soft_reboot") {
        execKsud("soft-reboot", true)
        return
    }
    val shell = getRootShell()
    val escaped = reason.replace("'", "'\\''")
    if (reason == "recovery") {
        // KEYCODE_POWER = 26, hide incorrect "Factory data reset" message
        ShellUtils.fastCmd(shell, "/system/bin/input keyevent 26")
    }
    ShellUtils.fastCmd(shell, "/system/bin/svc power reboot '$escaped' || /system/bin/reboot '$escaped'")
}

fun rootAvailable(): Boolean {
    val shell = getRootShell()
    return shell.isRoot
}

suspend fun getCurrentKmi(): String = withContext(Dispatchers.IO) {
    val shell = getRootShell()
    val cmd = "boot-info current-kmi"
    ShellUtils.fastCmd(shell, "${getKsuDaemonPath()} $cmd")
}

suspend fun getSupportedKmis(): List<String> = withContext(Dispatchers.IO) {
    val shell = getRootShell()
    val cmd = "boot-info supported-kmis"
    val out = shell.newJob().add("${getKsuDaemonPath()} $cmd").to(ArrayList(), null).exec().out
    out.filter { it.isNotBlank() }.map { it.trim() }
}

fun getSupportedKmisFromAssets(): List<String> {
    return try {
        ksuApp.assets.list("lkm")?.mapNotNull { name ->
            val lower = name.lowercase()
            when {
                lower.endsWith("_rekernel.ko") ->
                    name.substring(0, name.length - "_rekernel.ko".length)
                lower.endsWith("_kinsu.ko") ->
                    name.substring(0, name.length - "_kinsu.ko".length)
                else -> null
            }
        } ?: emptyList()
    } catch (_: Exception) {
        emptyList()
    }
}

suspend fun isAbDevice(): Boolean = withContext(Dispatchers.IO) {
    val shell = getRootShell()
    val cmd = "boot-info is-ab-device"
    ShellUtils.fastCmd(shell, "${getKsuDaemonPath()} $cmd").trim().toBoolean()
}

suspend fun getDefaultPartition(): String = withContext(Dispatchers.IO) {
    val shell = getRootShell()
    if (shell.isRoot) {
        val cmd = "boot-info default-partition"
        ShellUtils.fastCmd(shell, "${getKsuDaemonPath()} $cmd").trim()
    } else {
        if (!Os.uname().release.contains("android12-")) "init_boot" else "boot"
    }
}

suspend fun getSlotSuffix(ota: Boolean): String = withContext(Dispatchers.IO) {
    val shell = getRootShell()
    val cmd = if (ota) {
        "boot-info slot-suffix --ota"
    } else {
        "boot-info slot-suffix"
    }
    ShellUtils.fastCmd(shell, "${getKsuDaemonPath()} $cmd").trim()
}

suspend fun getAvailablePartitions(): List<String> = withContext(Dispatchers.IO) {
    val shell = getRootShell()
    val cmd = "boot-info available-partitions"
    val out = shell.newJob().add("${getKsuDaemonPath()} $cmd").to(ArrayList(), null).exec().out
    out.filter { it.isNotBlank() }.map { it.trim() }
}

fun hasMagisk(): Boolean {
    val shell = getRootShell(true)
    val result = shell.newJob().add("which magisk").exec()
    Log.i(TAG, "has magisk: ${result.isSuccess}")
    return result.isSuccess
}

fun isSepolicyValid(rules: String?): Boolean {
    if (rules.isNullOrBlank()) {
        return true
    }
    val shell = getRootShell()
    val escaped = rules.replace("'", "'\\''")
    val result =
        shell.newJob().add("${getKsuDaemonPath()} sepolicy check '$escaped'").to(ArrayList(), null)
            .exec()
    return result.isSuccess
}

fun getSepolicy(pkg: String): String {
    val shell = getRootShell()
    val escaped = pkg.replace("'", "'\\''")
    val result =
        shell.newJob().add("${getKsuDaemonPath()} profile get-sepolicy '$escaped'").to(ArrayList(), null)
            .exec()
    Log.i(TAG, "code: ${result.code}, out: ${result.out}, err: ${result.err}")
    return result.out.joinToString("\n")
}

fun setSepolicy(pkg: String, rules: String): Boolean {
    val shell = getRootShell()
    val escapedPkg = pkg.replace("'", "'\\''")
    val escapedRules = rules.replace("'", "'\\''")
    val result = shell.newJob().add("${getKsuDaemonPath()} profile set-sepolicy '$escapedPkg' '$escapedRules'")
        .to(ArrayList(), null).exec()
    Log.i(TAG, "set sepolicy result: ${result.code}")
    return result.isSuccess
}

fun listAppProfileTemplates(): List<String> {
    val shell = getRootShell()
    return shell.newJob().add("${getKsuDaemonPath()} profile list-templates").to(ArrayList(), null)
        .exec().out
}

fun getAppProfileTemplate(id: String): String {
    val shell = getRootShell()
    val escaped = id.replace("'", "'\\''")
    return shell.newJob().add("${getKsuDaemonPath()} profile get-template '$escaped'")
        .to(ArrayList(), null).exec().out.joinToString("\n")
}

fun setAppProfileTemplate(id: String, template: String): Boolean {
    val shell = getRootShell()
    val escapedId = id.replace("'", "'\\''")
    val escapedTemplate = template.replace("\"", "\\\"").replace("'", "'\\''")
    val cmd = """${getKsuDaemonPath()} profile set-template "$escapedId" "$escapedTemplate""""
    return shell.newJob().add(cmd)
        .to(ArrayList(), null).exec().isSuccess
}

fun deleteAppProfileTemplate(id: String): Boolean {
    val shell = getRootShell()
    val escaped = id.replace("'", "'\\''")
    return shell.newJob().add("${getKsuDaemonPath()} profile delete-template '$escaped'")
        .to(ArrayList(), null).exec().isSuccess
}

fun forceStopApp(packageName: String, userId: Int? = null) {
    val shell = getRootShell()
    val escaped = packageName.replace("'", "'\\''")
    val userArg = userId?.let { " --user $it" } ?: ""
    val result = shell.newJob().add("am force-stop$userArg '$escaped'").exec()
    Log.i(TAG, "force stop $packageName result: $result")
}

fun launchApp(packageName: String, userId: Int? = null) {
    val shell = getRootShell()
    val escaped = packageName.replace("'", "'\\''")
    val userArg = userId?.let { " --user $it" } ?: ""
    val result =
        shell.newJob()
            .add("cmd package resolve-activity --brief$userArg '$escaped' | tail -n 1 | xargs cmd activity start-activity$userArg -n")
            .exec()
    Log.i(TAG, "launch $packageName result: $result")
}

fun restartApp(packageName: String, userId: Int? = null) {
    forceStopApp(packageName, userId)
    launchApp(packageName, userId)
}

// SuSFS helpers
fun getSuSFSStatus(): String {
    val shell = getRootShell()
    return ShellUtils.fastCmd(shell, "${getKsuDaemonPath()} debug susfs status").trim()
}

fun getSuSFSVersion(): String {
    val shell = getRootShell()
    return ShellUtils.fastCmd(shell, "${getKsuDaemonPath()} debug susfs version").trim()
}

fun getSuSFSFeatures(): String {
    val shell = getRootShell()
    val cmd = "${getKsuDaemonPath()} debug susfs features"
    return ShellUtils.fastCmd(shell, cmd).trim()
}

suspend fun loadBundledLKM(): String = withContext(Dispatchers.IO) {
    var kmi = getCurrentKmi().trim()
    if (kmi.isBlank()) kmi = resolveKmiFromKernel() ?: ""
    if (kmi.isBlank()) return@withContext "Cannot detect KMI"
    // Sanitize kmi: only allow alphanumeric, dash, dot (e.g. "android15-6.6")
    val safeKmi = kmi.filter { it.isLetterOrDigit() || it == '-' || it == '.' }
    if (safeKmi != kmi) return@withContext "Invalid KMI format: $kmi"
    // Use ksud's built-in _kinsu.ko instead of potentially broken _rekernel.ko from assets
    val shell = getRootShell()
    val result = ShellUtils.fastCmd(shell, "${getKsuDaemonPath()} module load '$safeKmi' 2>&1").trim()
    if (result.isBlank()) "LKM loaded" else result
}

// ============================================================
// KPM module operations. The manager uses the standard top-level ksud KPM
// command, which maps to the single SukiSU-compatible KSU_IOCTL_KPM ABI.
// ============================================================

private val KPM_MODULE_ID = Regex("^[A-Za-z0-9][A-Za-z0-9._-]{0,30}$")

private fun shellQuote(value: String): String = "'${value.replace("'", "'\\''")}'"

fun isValidKpmModuleId(id: String): Boolean = KPM_MODULE_ID.matches(id)

private fun checkedShellOutput(command: String): String {
    val stdout = ArrayList<String>()
    val stderr = ArrayList<String>()
    val result = getRootShell().newJob().add(command).to(stdout, stderr).exec()
    check(result.isSuccess) {
        stderr.joinToString("\n").trim().ifBlank {
            "Command failed with exit code ${result.code}"
        }
    }
    return stdout.joinToString("\n").trim()
}

private fun kpmOutput(arguments: String): String =
    checkedShellOutput("${getKsuDaemonPath()} kpm $arguments")

fun kpmGetVersion(): String {
    return kpmOutput("version")
}

fun kpmGetNum(): Int {
    return runCatching { kpmOutput("num").toInt() }.getOrDefault(-1)
}

fun kpmListModules(): String {
    return kpmOutput("list")
}

fun kpmListPersistentModules(): List<String> {
    val output = checkedShellOutput(
        "if [ -d /data/adb/kpm ]; then " +
            "for f in /data/adb/kpm/*.kpm; do " +
            "[ -f \"\$f\" ] || continue; b=\${f##*/}; " +
            "printf '%s\\n' \"\${b%.kpm}\"; done; fi"
    )
    return output.lineSequence()
        .map(String::trim)
        .filter(::isValidKpmModuleId)
        .distinct()
        .toList()
}

fun kpmLoadModule(path: String, args: String = ""): Boolean {
    val shell = getRootShell()
    val argsPart = if (args.isBlank()) "" else " ${shellQuote(args)}"
    return ShellUtils.fastCmdResult(
        shell,
        "${getKsuDaemonPath()} kpm load ${shellQuote(path)}$argsPart"
    )
}

fun kpmUnloadModule(name: String): Boolean {
    if (!isValidKpmModuleId(name)) return false
    val shell = getRootShell()
    return ShellUtils.fastCmdResult(
        shell,
        "${getKsuDaemonPath()} kpm unload ${shellQuote(name)}"
    )
}

fun kpmGetModuleInfo(name: String): String {
    if (!isValidKpmModuleId(name)) return ""
    return kpmOutput("info ${shellQuote(name)}")
}

fun kpmControlModule(name: String, args: String = ""): Int {
    if (!isValidKpmModuleId(name)) return -1
    return runCatching {
        kpmOutput("control ${shellQuote(name)} ${shellQuote(args)}").toInt()
    }.getOrDefault(-1)
}

/**
 * Copy a validated KPM into the root-only staging/persistent directory.
 * Returns the root-visible destination path, or null when copying fails.
 */
fun kpmStageModule(sourcePath: String, moduleId: String, persistent: Boolean): String? {
    if (!isValidKpmModuleId(moduleId)) return null

    val suffix = if (persistent) {
        "$moduleId.kpm"
    } else {
        // Staging files deliberately do not use the .kpm extension: ksud only
        // scans *.kpm at boot, so a crash cannot turn a partial install into an
        // unexpected persistent module.
        ".kinsu-${System.currentTimeMillis()}-$moduleId.stage"
    }
    val destination = "/data/adb/kpm/$suffix"
    val temporary = "$destination.tmp"
    val command = buildString {
        append("mkdir -p /data/adb/kpm")
        append(" && chmod 700 /data/adb/kpm")
        append(" && cp ")
        append(shellQuote(sourcePath))
        append(' ')
        append(shellQuote(temporary))
        append(" && chmod 600 ")
        append(shellQuote(temporary))
        append(" && mv -f ")
        append(shellQuote(temporary))
        append(' ')
        append(shellQuote(destination))
    }

    val shell = getRootShell()
    return destination.takeIf { ShellUtils.fastCmdResult(shell, command) }
}

fun kpmRemovePersistentModule(moduleId: String): Boolean {
    if (!isValidKpmModuleId(moduleId)) return false
    val shell = getRootShell()
    return ShellUtils.fastCmdResult(
        shell,
        "rm -f ${shellQuote("/data/adb/kpm/$moduleId.kpm")}"
    )
}

fun kpmRemoveStagedModule(path: String) {
    if (!path.startsWith("/data/adb/kpm/.kinsu-")) return
    val shell = getRootShell()
    ShellUtils.fastCmdResult(shell, "rm -f ${shellQuote(path)}")
}

fun kpmPromoteStagedModule(path: String, moduleId: String): Boolean {
    if (!path.startsWith("/data/adb/kpm/.kinsu-") || !isValidKpmModuleId(moduleId)) {
        return false
    }
    val destination = "/data/adb/kpm/$moduleId.kpm"
    val shell = getRootShell()
    return ShellUtils.fastCmdResult(
        shell,
        "chmod 600 ${shellQuote(path)} && mv -f ${shellQuote(path)} ${shellQuote(destination)}"
    )
}

fun kpmIsPersistent(moduleId: String): Boolean {
    if (!isValidKpmModuleId(moduleId)) return false
    val shell = getRootShell()
    return ShellUtils.fastCmdResult(
        shell,
        "test -f ${shellQuote("/data/adb/kpm/$moduleId.kpm")}"
    )
}
