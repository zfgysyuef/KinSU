package com.mikokernel.data.repository

import com.mikokernel.data.model.RepoModule

interface ModuleRepoRepository {
    suspend fun fetchModules(): Result<List<RepoModule>>
}
