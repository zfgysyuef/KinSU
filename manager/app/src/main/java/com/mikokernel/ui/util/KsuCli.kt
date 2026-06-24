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
        val scriptWithEnv = buildString {
            appendLine("export ASH_STANDALONE=1")
            appendLine("export KSU=true")
            appendLine("export KSU_KERNEL_VER_CODE=\$(cat /proc/version 2>/dev/null | head -1 || echo unknown)")
            appendLine("export KSU_VER_CODE=$verCode")
            appendLine("export KSU_VER=$verName")
            appendLine("export PATH=/system/bin:/data/adb/ksu/bin:\$PATH")
            appendLine("export OUTFD=1")
            appendLine("export ZIPFILE=/data/local/tmp/kmodule_$suffix.zip")
            appendLine()
            append(installerScript)
            appendLine()
            // Override MAGISK_VER after installer.sh (it hardcodes 25.2)
            appendLine("export MAGISK_VER=KinSu")
            appendLine("export MAGISK_VER_CODE=$verCode")
            appendLine("install_module")
            appendLine("rm -f /data/local/tmp/install_$suffix.sh /data/local/tmp/kmodule_$suffix.zip")
            appendLine("exit 0")
        }
        val installSh = File(ksuApp.cacheDir, "install_$suffix.sh")
        installSh.writeText(scriptWithEnv, Charsets.UTF_8)

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
            val bootFile = File(ksuApp.cacheDir, "boot.img")
            bootFile.outputStream().use { output ->
                this?.copyTo(output)
            }

            bootFile
        }
    }

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
        cmd += " --enable-kpm"
    }

    if (enableSusfs) {
        cmd += " --enable-susfs"
    }

    if (ota) {
        cmd += " -u"
    }

    var lkmFile: File? = null
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
// KPM 模块操作 — 通过 ksud debug kpm 调用内核桩函数
// 仅在 KPM/GKI 模式下使用（kpimg 已加载时桩函数被 hook）。
// ============================================================

/** 获取 KernelPatch 版本 */
fun kpmGetVersion(): String {
    val shell = getRootShell()
    return ShellUtils.fastCmd(shell, "${getKsuDaemonPath()} debug kpm version").trim()
}

/** 获取已加载 KPM 模块数量 */
fun kpmGetNum(): Int {
    val shell = getRootShell()
    val result = ShellUtils.fastCmd(shell, "${getKsuDaemonPath()} debug kpm num").trim()
    return result.toIntOrNull() ?: -1
}

/** 列出所有 KPM 模块 (JSON) */
fun kpmListModules(): String {
    val shell = getRootShell()
    val result = ShellUtils.fastCmd(shell, "${getKsuDaemonPath()} debug kpm list").trim()
    return result.ifBlank { "[]" }
}

/** 加载 KPM 模块 */
fun kpmLoadModule(path: String, args: String = ""): Boolean {
    val shell = getRootShell()
    val escapedPath = path.replace("'", "'\\''")
    val cmd = if (args.isNotBlank()) {
        val escapedArgs = args.replace("'", "'\\''")
        "${getKsuDaemonPath()} debug kpm load '$escapedPath' '$escapedArgs'"
    } else {
        "${getKsuDaemonPath()} debug kpm load '$escapedPath'"
    }
    val result = ShellUtils.fastCmd(shell, cmd).trim()
    Log.i(TAG, "KPM load module $path result: $result")
    return result == "OK"
}

/** 卸载 KPM 模块 */
fun kpmUnloadModule(name: String): Boolean {
    val shell = getRootShell()
    val escaped = name.replace("'", "'\\''")
    val result = ShellUtils.fastCmd(shell, "${getKsuDaemonPath()} debug kpm unload '$escaped'").trim()
    Log.i(TAG, "KPM unload module $name result: $result")
    return result == "OK"
}

/** 获取 KPM 模块信息 */
fun kpmGetModuleInfo(name: String): String {
    val shell = getRootShell()
    val escaped = name.replace("'", "'\\''")
    return ShellUtils.fastCmd(shell, "${getKsuDaemonPath()} debug kpm info '$escaped'").trim()
}

/** 发送控制命令到 KPM 模块 */
fun kpmControlModule(name: String, args: String = ""): Int {
    val shell = getRootShell()
    val escapedName = name.replace("'", "'\\''")
    val cmd = if (args.isNotBlank()) {
        val escapedArgs = args.replace("'", "'\\''")
        "${getKsuDaemonPath()} debug kpm control '$escapedName' '$escapedArgs'"
    } else {
        "${getKsuDaemonPath()} debug kpm control '$escapedName'"
    }
    val result = ShellUtils.fastCmd(shell, cmd).trim()
    Log.i(TAG, "KPM control $name result: $result")
    return if (result == "OK") 0 else -1
}

/** 安装 KPM 模块 (ZIP/.kpm) — 从 ZIP 中提取 .kpm 文件后通过 kpm load 加载 */
fun kpmInstallModule(
    uri: Uri,
    onStdout: (String) -> Unit,
    onStderr: (String) -> Unit
): FlashResult {
    val resolver = ksuApp.contentResolver
    with(resolver.openInputStream(uri)) {
        // Get original filename from URI (sanitize to prevent path traversal)
        val rawDisplayName = uri.lastPathSegment?.substringAfterLast('/') ?: "kpm_module"
        val displayName = File(rawDisplayName).name // strip any ../ path components
        val isKpmFile = displayName.endsWith(".kpm", ignoreCase = true)
        val savedFile = File(ksuApp.cacheDir, if (isKpmFile) displayName else "kpm_module.zip")
        savedFile.outputStream().use { output ->
            this?.copyTo(output)
        }

        // If the selected file is already a .kpm, load it directly
        var kpmFile: File? = if (isKpmFile) savedFile else null

        // If it's a ZIP, try to extract .kpm from it
        if (!isKpmFile) {
            try {
                val zip = java.util.zip.ZipFile(savedFile)
                val entry = zip.entries().asSequence().find {
                    it.name.endsWith(".kpm", ignoreCase = true) && !it.isDirectory
                }
                if (entry != null) {
                    val extractedFile = File(ksuApp.cacheDir, entry.name.substringAfterLast('/'))
                    kpmFile = extractedFile
                    zip.getInputStream(entry).use { input ->
                        extractedFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    onStdout("提取: ${entry.name}")
                } else {
                    onStdout("未找到 .kpm 文件，尝试直接加载")
                }
                zip.close()
            } catch (e: Exception) {
                onStderr("解压失败: ${e.message}")
            }
        }

        val moduleFile = kpmFile ?: savedFile.also {
            onStdout("未提取到 .kpm，使用原文件")
        }

        // Copy to /data/adb/ via root shell so ksud (root) can read it
        // /data/adb/ is the standard KernelSU directory and always root-accessible
        val destName = "kpm_${System.currentTimeMillis()}.kpm"
        val rootPath = "/data/adb/$destName"
        val copyResult = withNewRootShell(true) {
            newJob().add("mkdir -p /data/adb").to(ArrayList(), ArrayList()).exec()
            newJob().add("cp '${moduleFile.absolutePath}' '$rootPath' && chmod 644 '$rootPath' && ls -la '$rootPath'")
                .to(object : CallbackList<String?>() {
                    override fun onAddElement(s: String?) { if (s != null) onStdout(s) }
                }, object : CallbackList<String?>() {
                    override fun onAddElement(s: String?) { if (s != null) onStderr(s) }
                }).exec()
        }
        val loadPath = if (copyResult.isSuccess) rootPath else moduleFile.absolutePath
        val escapedLoadPath = loadPath.replace("'", "'\\''")
        val result = flashWithIO("${getKsuDaemonPath()} debug kpm load '$escapedLoadPath'", onStdout, onStderr)
        Log.i("KinSU", "KPM install module $uri result: code=${result.code} out=${result.out}")

        // Cleanup
        withNewRootShell(true) {
            newJob().add("rm -f '$rootPath'").to(ArrayList(), ArrayList()).exec()
        }
        savedFile.delete()
        if (moduleFile != savedFile) moduleFile.delete()

        return FlashResult(result)
    }
}

/** 卸载 KPM 模块（通过 kpm unload） */
fun kpmUninstallModule(id: String): Boolean {
    return kpmUnloadModule(id)
}

/** 启用/禁用 KPM 模块（通过 kpm control） */
fun kpmToggleModule(id: String, enable: Boolean): Boolean {
    val cmd = if (enable) "enable" else "disable"
    return kpmControlModule(id, cmd) == 0
}

/** 运行 KPM 模块 action */
fun kpmRunModuleAction(
    moduleId: String, onStdout: (String) -> Unit, onStderr: (String) -> Unit
): Boolean {
    val escapedId = moduleId.replace("'", "'\\''")
    val stdoutCallback: CallbackList<String?> = object : CallbackList<String?>() {
        override fun onAddElement(s: String?) { onStdout(s ?: "") }
    }
    val stderrCallback: CallbackList<String?> = object : CallbackList<String?>() {
        override fun onAddElement(s: String?) { onStderr(s ?: "") }
    }
    val result = withNewRootShell(true) {
        newJob().add("${getKsuDaemonPath()} debug kpm control '$escapedId' 'action'")
            .to(stdoutCallback, stderrCallback).exec()
    }
    Log.i("KinSU", "KPM module action result: $result")
    return result.isSuccess
}

/** 获取 KPM 模块配置 */
fun kpmGetModuleConfig(moduleId: String, key: String): String {
    val escapedKey = key.replace("'", "'\\''")
    return kpmControlModule(moduleId, "config get '$escapedKey'").toString()
}

/** 设置 KPM 模块配置 */
fun kpmSetModuleConfig(moduleId: String, key: String, value: String, temp: Boolean = false): Boolean {
    val escapedKey = key.replace("'", "'\\''")
    val escapedValue = value.replace("'", "'\\''")
    val tempFlag = if (temp) " --temp" else ""
    return kpmControlModule(moduleId, "config set '$escapedKey' '$escapedValue'$tempFlag") == 0
}

/** 触发 post-fs-data 事件 */
fun kpmPostFsData(superkey: String? = null): Boolean {
    val keyArg = superkey?.let { " --superkey '${it.replace("'", "'\\''")}'" } ?: ""
    val shell = getRootShell()
    return ShellUtils.fastCmdResult(shell, "${getKsuDaemonPath()} debug kpm control 'post-fs-data'$keyArg")
}

/** 触发 boot-completed 事件 */
fun kpmBootCompleted(superkey: String? = null): Boolean {
    val keyArg = superkey?.let { " --superkey '${it.replace("'", "'\\''")}'" } ?: ""
    val shell = getRootShell()
    return ShellUtils.fastCmdResult(shell, "${getKsuDaemonPath()} debug kpm control 'boot-completed'$keyArg")
}
