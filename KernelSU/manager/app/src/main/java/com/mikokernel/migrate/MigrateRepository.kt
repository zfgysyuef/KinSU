package com.mikokernel.migrate

import android.util.Log
import com.mikokernel.ui.util.execKsud
import com.mikokernel.ui.util.getRootShell
import com.mikokernel.ui.util.withNewRootShell
import com.topjohnwu.superuser.ShellUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

private const val TAG = "MigrateRepository"

/**
 * Repository for migration operations. Communicates with ksud via root shell.
 */
class MigrateRepository {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    /**
     * Detect conflicts from other root managers.
     * @param mask bitmask: modules=1, init=2, sepolicy=4, mount=8, boot=16; 0=all
     */
    suspend fun detectConflicts(mask: Int = 0x1F): List<ConflictItem> = withContext(Dispatchers.IO) {
        try {
            val shell = getRootShell()
            val out = shell.newJob()
                .add("${getDaemonPath()} migrate detect --mask $mask --json")
                .to(ArrayList<String>(), null)
                .exec()
                .out

            val jsonStr = out.joinToString("\n").trim()
            if (jsonStr.isNotBlank() && jsonStr.startsWith("[")) {
                json.decodeFromString<List<ConflictItem>>(jsonStr)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "detectConflicts failed", e)
            emptyList()
        }
    }

    /**
     * Clean detected conflicts.
     */
    suspend fun cleanConflicts(mask: Int = 0x1F, backup: Boolean = true): CleanResult = withContext(Dispatchers.IO) {
        try {
            val shell = getRootShell()
            val backupFlag = if (backup) "--backup" else "--no-backup"
            val out = shell.newJob()
                .add("${getDaemonPath()} migrate clean --mask $mask $backupFlag --yes --json")
                .to(ArrayList<String>(), null)
                .exec()
                .out

            val jsonStr = out.joinToString("\n").trim()
            if (jsonStr.isNotBlank() && jsonStr.startsWith("{")) {
                json.decodeFromString<CleanResult>(jsonStr)
            } else {
                CleanResult(success = false, errors = listOf("Failed to parse clean result"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "cleanConflicts failed", e)
            CleanResult(success = false, errors = listOf(e.message ?: "Unknown error"))
        }
    }

    /**
     * Migrate modules from a specific manager.
     */
    suspend fun migrateFrom(source: MigrationSource, preserveData: Boolean = false): MigrateResult =
        withContext(Dispatchers.IO) {
            try {
                val shell = getRootShell()
                val preserveFlag = if (preserveData) "--preserve-data" else ""
                val cmd = when (source) {
                    MigrationSource.MAGISK -> "migrate from-magisk $preserveFlag --json"
                    MigrationSource.APATCH -> "migrate from-apatch $preserveFlag --json"
                    MigrationSource.OLDKSU -> "migrate from-old-ksu $preserveFlag --json"
                }

                val out = shell.newJob()
                    .add("${getDaemonPath()} $cmd")
                    .to(ArrayList<String>(), null)
                    .exec()
                    .out

                val jsonStr = out.joinToString("\n").trim()
                if (jsonStr.isNotBlank() && jsonStr.startsWith("{")) {
                    json.decodeFromString<MigrateResult>(jsonStr)
                } else {
                    MigrateResult(
                        success = false,
                        source = source.id,
                        errors = listOf("Failed to parse migration result")
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "migrateFrom failed", e)
                MigrateResult(
                    success = false,
                    source = source.id,
                    errors = listOf(e.message ?: "Unknown error")
                )
            }
        }

    /**
     * List existing backups.
     */
    suspend fun listBackups(): List<String> = withContext(Dispatchers.IO) {
        try {
            val shell = getRootShell()
            val out = shell.newJob()
                .add("${getDaemonPath()} migrate backups")
                .to(ArrayList<String>(), null)
                .exec()
                .out

            out.filter { it.isNotBlank() && !it.startsWith("No backups") }
                .map { it.trim() }
        } catch (e: Exception) {
            Log.e(TAG, "listBackups failed", e)
            emptyList()
        }
    }

    private fun getDaemonPath(): String {
        val appInfo = com.mikokernel.ksuApp.applicationInfo
        return appInfo.nativeLibraryDir + "/libkinsud.so"
    }
}
