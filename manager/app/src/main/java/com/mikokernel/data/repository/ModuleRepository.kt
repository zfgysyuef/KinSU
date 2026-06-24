package com.mikokernel.data.repository

import com.mikokernel.data.model.Module
import com.mikokernel.data.model.ModuleUpdateInfo

interface ModuleRepository {
    suspend fun getModules(): Result<List<Module>>
    suspend fun checkUpdate(module: Module): Result<ModuleUpdateInfo>
}
