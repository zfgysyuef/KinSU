package com.mikokernel.ui.util

import android.content.Context
import android.net.Uri
import com.topjohnwu.superuser.Shell
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AnyKernel3FlashLog(val text: String, val progress: Float = 0f, val step: String = "")

class AnyKernel3Worker(
    private val context: Context,
    private val kernelUri: Uri,
    private val selectedSlot: String? = null,
    private val onLog: (AnyKernel3FlashLog) -> Unit = {},
    private val onComplete: () -> Unit = {},
    private val onError: (String) -> Unit = {},
) : Thread() {

    override fun run() {
        val workDir = context.filesDir.absolutePath
        try {
            onLog(AnyKernel3FlashLog("Preparing...", 0.05f, "Cleaning workspace"))
            runCommand(false, "find $workDir -type f ! -name '*.jpg' ! -name '*.png' -delete")
            runCommand(false, "rm -rf $workDir/work $workDir/META-INF")

            onLog(AnyKernel3FlashLog("Copying ZIP...", 0.1f, "Copying"))
            val zipPath = "$workDir/tmp_kernel.zip"
            context.contentResolver.openInputStream(kernelUri)?.use { input ->
                FileOutputStream(File(zipPath)).use { output -> input.copyTo(output) }
            }

            onLog(AnyKernel3FlashLog("Extracting update-binary...", 0.25f, "Extracting"))
            runCommand(false, "unzip -o \"$zipPath\" \"*/update-binary\" -d $workDir")
            val binaryPath = "$workDir/META-INF/com/google/android/update-binary"
            if (!File(binaryPath).exists()) {
                onError("Not a valid AnyKernel3 ZIP (missing update-binary)")
                return
            }
            File(binaryPath).setExecutable(true)

            onLog(AnyKernel3FlashLog("Patching script...", 0.4f, "Patching"))
            val kernelVersion = Shell.cmd("cat /proc/version").exec().out.joinToString("\n")
            val ver = Regex("""\d+\.\d+\.\d+""").find(kernelVersion)?.value ?: "5.15.0"
            val parts = ver.split(".")
            val major = parts.getOrNull(0)?.toIntOrNull() ?: 5
            val minor = parts.getOrNull(1)?.toIntOrNull() ?: 15
            val toolAsset = if (major < 5 || (major == 5 && minor <= 10)) "5_10-mkbootfs" else "5_15+-mkbootfs"
            val toolPath = "$workDir/mkbootfs"
            context.assets.open(toolAsset).use { input ->
                FileOutputStream(File(toolPath)).use { output -> input.copyTo(output) }
            }
            File(toolPath).setExecutable(true)
            runCommand(false, "sed -i '/chmod -R 755 tools bin;/i cp -f $toolPath \$AKHOME/tools;' $binaryPath")

            val isAb = isAbDevice()
            var origSlot: String? = null

            if (isAb && selectedSlot != null) {
                onLog(AnyKernel3FlashLog("Setting target slot: _$selectedSlot", 0.45f))
                origSlot = Shell.cmd("getprop ro.boot.slot_suffix").exec().out.firstOrNull()?.trim()
                runCommand(true, "resetprop -n ro.boot.slot_suffix _$selectedSlot")
            }

            onLog(AnyKernel3FlashLog("Flashing kernel...", 0.5f, "Flashing"))
            val process = ProcessBuilder("su").redirectErrorStream(true).start()
            try {
                process.outputStream.bufferedWriter().use { writer ->
                    writer.write("export POSTINSTALL=$workDir\n")
                    selectedSlot?.let { writer.write("echo \"$it\" > $workDir/bootslot\n") }
                    val cmd = buildString {
                        append("sh $binaryPath 3 1 \"$zipPath\"")
                        if (selectedSlot != null) append(" \"\$(cat $workDir/bootslot)\"")
                        append(" && touch $workDir/done\n")
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

            if (!File("$workDir/done").exists()) {
                val isStillAb = isAbDevice()
                if (isStillAb && !origSlot.isNullOrEmpty()) {
                    runCommand(true, "resetprop ro.boot.slot_suffix $origSlot")
                }
                onError("Flash failed (done marker not found)")
                return
            }

            if (isAb && !origSlot.isNullOrEmpty()) {
                onLog(AnyKernel3FlashLog("Restoring original slot...", 0.92f))
                runCommand(true, "resetprop ro.boot.slot_suffix $origSlot")
            }

            onLog(AnyKernel3FlashLog("Flash complete!", 1f))
            onComplete()
        } catch (e: Exception) {
            onError(e.message ?: "Unknown error")
        }
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
