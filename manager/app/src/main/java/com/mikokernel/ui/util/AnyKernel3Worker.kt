package com.mikokernel.ui.util

import android.content.Context
import android.net.Uri
import com.topjohnwu.superuser.Shell
import java.io.File
import java.io.FileOutputStream
import com.mikokernel.R

data class AnyKernel3FlashLog(val text: String, val progress: Float = 0f, val step: String = "")

class AnyKernel3Worker(
    private val context: Context,
    private val kernelUri: Uri,
    private val selectedSlot: String? = null,
    private val onLog: (AnyKernel3FlashLog) -> Unit = {},
    private val onComplete: () -> Unit = {},
    private val onError: (String) -> Unit = {},
) : Thread() {

    private val workDir = context.filesDir.absolutePath
    private val zipPath = "$workDir/tmp_kernel.zip"
    private val binaryPath = "$workDir/META-INF/com/google/android/update-binary"
    private val toolPath = "$workDir/mkbootfs"
    private val doneMarker = "$workDir/done"
    private val slotFile = "$workDir/bootslot"

    override fun run() {
        try {
            onLog(AnyKernel3FlashLog(context.getString(R.string.anykernel3_preparing), 0.05f, context.getString(R.string.anykernel3_cleaning_workspace)))
            cleanupWorkspace()

            onLog(AnyKernel3FlashLog(context.getString(R.string.anykernel3_copying_zip), 0.1f, context.getString(R.string.anykernel3_copying)))
            context.contentResolver.openInputStream(kernelUri)?.use { input ->
                FileOutputStream(File(zipPath)).use { output -> input.copyTo(output) }
            }

            onLog(AnyKernel3FlashLog(context.getString(R.string.anykernel3_extracting_update_binary), 0.25f, context.getString(R.string.anykernel3_extracting)))
            runCommand(false, "unzip -o \"$zipPath\" \"*/update-binary\" -d $workDir")
            if (!File(binaryPath).exists()) {
                onError(context.getString(R.string.anykernel3_invalid_zip))
                cleanupArtifacts()
                return
            }
            File(binaryPath).setExecutable(true)

            onLog(AnyKernel3FlashLog(context.getString(R.string.anykernel3_patching_script), 0.4f, context.getString(R.string.anykernel3_patching)))
            val kernelVersion = Shell.cmd("cat /proc/version").exec().out.joinToString("\n")
            val ver = Regex("""\d+\.\d+\.\d+""").find(kernelVersion)?.value ?: "5.15.0"
            val parts = ver.split(".")
            val major = parts.getOrNull(0)?.toIntOrNull() ?: 5
            val minor = parts.getOrNull(1)?.toIntOrNull() ?: 15
            val toolAsset = if (major < 5 || (major == 5 && minor <= 10)) "5_10-mkbootfs" else "5_15+-mkbootfs"
            context.assets.open(toolAsset).use { input ->
                FileOutputStream(File(toolPath)).use { output -> input.copyTo(output) }
            }
            File(toolPath).setExecutable(true)
            runCommand(false, "sed -i '/chmod -R 755 tools bin;/i cp -f $toolPath \$AKHOME/tools;' $binaryPath")

            val isAb = isAbDevice()
            var origSlot: String? = null

            if (isAb && selectedSlot != null) {
                // Validate selectedSlot: only allow alphanumeric and underscore
                val safeSlot = selectedSlot.filter { it.isLetterOrDigit() || it == '_' }
                if (safeSlot != selectedSlot) {
                    onError("Invalid slot name: $selectedSlot")
                    cleanupArtifacts()
                    return
                }
                onLog(AnyKernel3FlashLog(context.getString(R.string.anykernel3_setting_target_slot, safeSlot), 0.45f))
                origSlot = Shell.cmd("getprop ro.boot.slot_suffix").exec().out.firstOrNull()?.trim()
                runCommand(true, "resetprop -n ro.boot.slot_suffix _$safeSlot")
            }

            onLog(AnyKernel3FlashLog(context.getString(R.string.anykernel3_flashing_kernel), 0.5f, context.getString(R.string.anykernel3_flashing)))
            val process = ProcessBuilder("su").redirectErrorStream(true).start()
            try {
                process.outputStream.bufferedWriter().use { writer ->
                    writer.write("export POSTINSTALL=$workDir\n")
                    selectedSlot?.let { slot ->
                        val safeSlot = slot.filter { c -> c.isLetterOrDigit() || c == '_' }
                        writer.write("echo \"$safeSlot\" > $slotFile\n")
                    }
                    val cmd = buildString {
                        append("sh $binaryPath 3 1 \"$zipPath\"")
                        if (selectedSlot != null) append(" \"\$(cat $slotFile)\"")
                        append(" && touch $doneMarker\n")
                    }
                    writer.write(cmd)
                    writer.write("exit\n")
                    writer.flush()
                }
                val logs = StringBuilder()
                process.inputStream.bufferedReader().use { reader ->
                    reader.lineSequence().forEach { line ->
                        if (line.startsWith("ui_print")) {
                            val msg = line.removePrefix("ui_print").trim()
                            logs.appendLine(msg)
                            var p = 0.55f
                            if (msg.contains("extracting", true)) p = 0.6f
                            if (msg.contains("installing", true)) p = 0.75f
                            if (msg.contains("unpacking", true)) p = 0.7f
                            if (msg.contains("complete", true)) p = 0.9f
                            onLog(AnyKernel3FlashLog(msg, p))
                        }
                    }
                }
            } finally { process.destroy() }

            if (!File(doneMarker).exists()) {
                val isStillAb = isAbDevice()
                if (isStillAb && !origSlot.isNullOrEmpty()) {
                    runCommand(true, "resetprop ro.boot.slot_suffix $origSlot")
                }
                onError(context.getString(R.string.anykernel3_flash_failed_done_marker))
                cleanupArtifacts()
                return
            }

            if (isAb && !origSlot.isNullOrEmpty()) {
                onLog(AnyKernel3FlashLog(context.getString(R.string.anykernel3_restoring_original_slot), 0.92f))
                runCommand(true, "resetprop ro.boot.slot_suffix $origSlot")
            }

            onLog(AnyKernel3FlashLog(context.getString(R.string.anykernel3_flash_complete), 1f))
            cleanupArtifacts()
            onComplete()
        } catch (e: Exception) {
            cleanupArtifacts()
            onError(e.message ?: context.getString(R.string.anykernel3_unknown_error))
        }
    }

    private fun cleanupWorkspace() {
        runCommand(false, "find $workDir -type f ! -name '*.jpg' ! -name '*.png' -delete")
        runCommand(false, "rm -rf $workDir/work $workDir/META-INF")
    }

    private fun cleanupArtifacts() {
        runCommand(false, "rm -f $zipPath $toolPath $doneMarker $slotFile")
        runCommand(false, "rm -rf $workDir/work $workDir/META-INF")
    }

    private fun isAbDevice(): Boolean {
        val ab = Shell.cmd("getprop ro.build.ab_update").exec().out.firstOrNull()?.trim() ?: "false"
        if (ab.lowercase() != "true") return false
        val slot = Shell.cmd("getprop ro.boot.slot_suffix").exec().out.firstOrNull()?.trim() ?: ""
        return slot.isNotEmpty()
    }

    private fun runCommand(su: Boolean, cmd: String) {
        val sh = if (su) "su" else "sh"
        val p = Runtime.getRuntime().exec(arrayOf(sh, "-c", cmd))
        p.waitFor()
        p.destroy()
    }
}
