package com.mikokernel.data.repository

import com.mikokernel.data.model.KpmFileMetadata
import com.mikokernel.data.model.KpmModuleInfo
import java.io.File

interface KpmRepository {
    suspend fun getVersion(): Result<String>
    suspend fun getModuleList(): Result<List<KpmModuleInfo>>
    suspend fun install(file: File, metadata: KpmFileMetadata, persistent: Boolean): Result<Unit>
    suspend fun uninstall(moduleId: String): Result<Unit>
    suspend fun control(moduleId: String, args: String): Result<Int>
}
