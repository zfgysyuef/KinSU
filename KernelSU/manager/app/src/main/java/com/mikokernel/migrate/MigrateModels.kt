package com.mikokernel.migrate

import kotlinx.serialization.Serializable

/**
 * A single detected conflict item from another root manager.
 */
@Serializable
data class ConflictItem(
    val path: String,
    val source: String,   // "magisk", "apatch", "oldksu", "system"
    val type: String      // "modules", "init", "sepolicy", "mount"
)

/**
 * Result of a clean operation.
 */
@Serializable
data class CleanResult(
    val success: Boolean,
    val backupPath: String? = null,
    val cleanedCount: Int = 0,
    val failedCount: Int = 0,
    val errors: List<String> = emptyList(),
    val details: List<CleanDetail> = emptyList()
)

@Serializable
data class CleanDetail(
    val path: String,
    val action: String,
    val success: Boolean,
    val error: String? = null
)

/**
 * Result of a migration operation.
 */
@Serializable
data class MigrateResult(
    val success: Boolean,
    val source: String,
    val detectedCount: Int = 0,
    val migratedCount: Int = 0,
    val skippedCount: Int = 0,
    val failedCount: Int = 0,
    val cleanResult: CleanResult? = null,
    val details: List<ModuleMigrateDetail> = emptyList(),
    val errors: List<String> = emptyList()
)

@Serializable
data class ModuleMigrateDetail(
    val moduleId: String,
    val sourcePath: String,
    val destPath: String? = null,
    val status: String,  // "migrated", "skipped", "failed"
    val reason: String? = null
)

/**
 * Migration source manager types.
 */
enum class MigrationSource(val id: String, val displayName: String) {
    MAGISK("magisk", "Magisk"),
    APATCH("apatch", "APatch"),
    OLDKSU("oldksu", "KernelSU (旧版)");

    companion object {
        fun fromId(id: String): MigrationSource? = entries.find { it.id == id }
    }
}
