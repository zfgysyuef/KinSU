package com.mikokernel.data.repository

import com.mikokernel.data.model.KpmFileMetadata
import com.mikokernel.data.model.KpmModuleInfo
import com.mikokernel.ui.util.isValidKpmModuleId
import com.mikokernel.ui.util.kpmControlModule
import com.mikokernel.ui.util.kpmGetModuleInfo
import com.mikokernel.ui.util.kpmGetVersion
import com.mikokernel.ui.util.kpmListModules
import com.mikokernel.ui.util.kpmListPersistentModules
import com.mikokernel.ui.util.kpmLoadModule
import com.mikokernel.ui.util.kpmPromoteStagedModule
import com.mikokernel.ui.util.kpmRemovePersistentModule
import com.mikokernel.ui.util.kpmRemoveStagedModule
import com.mikokernel.ui.util.kpmStageModule
import com.mikokernel.ui.util.kpmUnloadModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

class KpmRepositoryImpl : KpmRepository {
    private val operationMutex = Mutex()

    override suspend fun getVersion(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            kpmGetVersion().takeIf { it.isNotBlank() }
                ?: error("KernelPatch KPM bridge is not active")
        }
    }

    override suspend fun getModuleList(): Result<List<KpmModuleInfo>> =
        withContext(Dispatchers.IO) {
            operationMutex.withLock {
                runCatching {
                    val loadedIds = kpmListModules()
                        .lineSequence()
                        .map(String::trim)
                        .filter(String::isNotEmpty)
                        .filter(::isValidKpmModuleId)
                        .distinct()
                        .toSet()
                    val persistentIds = kpmListPersistentModules().toSet()

                    (loadedIds + persistentIds)
                        .sorted()
                        .map { id ->
                            readModuleInfo(
                                id = id,
                                loaded = id in loadedIds,
                                persistent = id in persistentIds,
                            )
                        }
                }
            }
        }

    override suspend fun install(
        file: File,
        metadata: KpmFileMetadata,
        persistent: Boolean,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        operationMutex.withLock {
            runCatching {
                require(isValidKpmModuleId(metadata.id)) { "Unsafe KPM module name" }

                val alreadyLoaded = kpmListModules().lineSequence().any {
                    it.trim() == metadata.id
                }
                check(!alreadyLoaded) {
                    "KPM module ${metadata.id} is already loaded; uninstall it before replacing it"
                }

                val stagedPath = kpmStageModule(
                    sourcePath = file.absolutePath,
                    moduleId = metadata.id,
                    persistent = false,
                ) ?: error("Unable to stage KPM module in /data/adb/kpm")

                var promoted = false
                var loadedNewModule = false
                try {
                    check(kpmLoadModule(stagedPath, metadata.args)) {
                        "Kernel rejected the KPM module"
                    }
                    loadedNewModule = true

                    val loadedInfo = parseModuleInfo(kpmGetModuleInfo(metadata.id))
                    check(loadedInfo["name"] == metadata.id) {
                        "Loaded KPM identity does not match ${metadata.id}"
                    }

                    if (persistent) {
                        check(kpmPromoteStagedModule(stagedPath, metadata.id)) {
                            "KPM loaded, but persistent installation failed"
                        }
                        promoted = true
                    }
                } catch (error: Throwable) {
                    if (loadedNewModule && !kpmUnloadModule(metadata.id)) {
                        throw IllegalStateException(
                            "${error.message.orEmpty()}; failed to unload the partially installed KPM",
                            error,
                        )
                    }
                    throw error
                } finally {
                    if (!promoted) {
                        kpmRemoveStagedModule(stagedPath)
                    }
                }
            }
        }
    }

    override suspend fun uninstall(moduleId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            operationMutex.withLock {
                runCatching {
                    require(isValidKpmModuleId(moduleId)) { "Unsafe KPM module name" }
                    val loaded = kpmListModules().lineSequence().any {
                        it.trim() == moduleId
                    }
                    if (loaded) {
                        check(kpmUnloadModule(moduleId)) { "Unable to unload KPM module" }
                    }
                    check(kpmRemovePersistentModule(moduleId)) {
                        "Unable to remove persistent KPM module"
                    }
                }
            }
        }

    override suspend fun control(moduleId: String, args: String): Result<Int> =
        withContext(Dispatchers.IO) {
            operationMutex.withLock {
                runCatching {
                    require(isValidKpmModuleId(moduleId)) { "Unsafe KPM module name" }
                    check(kpmListModules().lineSequence().any { it.trim() == moduleId }) {
                        "KPM module is not loaded"
                    }
                    kpmControlModule(moduleId, args).also {
                        check(it >= 0) { "KPM control call failed" }
                    }
                }
            }
        }

    private fun parseModuleInfo(info: String): Map<String, String> =
        info
            .lineSequence()
            .mapNotNull { line ->
                val separator = line.indexOf('=')
                if (separator <= 0) null
                else line.substring(0, separator).trim() to
                    line.substring(separator + 1).trim()
            }
            .toMap()

    private fun readModuleInfo(
        id: String,
        loaded: Boolean,
        persistent: Boolean,
    ): KpmModuleInfo {
        val properties = if (loaded) {
            parseModuleInfo(kpmGetModuleInfo(id))
        } else {
            emptyMap()
        }

        return KpmModuleInfo(
            id = id,
            name = properties["name"].orEmpty().ifBlank { id },
            version = properties["version"].orEmpty(),
            license = properties["license"].orEmpty(),
            author = properties["author"].orEmpty(),
            description = properties["description"].orEmpty(),
            args = properties["args"].orEmpty(),
            loaded = loaded,
            persistent = persistent,
        )
    }
}
