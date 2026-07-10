package com.mikokernel.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikokernel.data.model.KpmFileMetadata
import com.mikokernel.data.repository.KpmRepository
import com.mikokernel.data.repository.KpmRepositoryImpl
import com.mikokernel.ui.screen.kpm.KpmUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class KpmViewModel(
    private val repository: KpmRepository = KpmRepositoryImpl(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(KpmUiState(isRefreshing = true))
    val uiState: StateFlow<KpmUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }

            val version = repository.getVersion()
            if (version.isFailure) {
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        version = "",
                        modules = emptyList(),
                        error = version.exceptionOrNull()?.message,
                    )
                }
                return@launch
            }

            repository.getModuleList()
                .onSuccess { modules ->
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            version = version.getOrThrow(),
                            modules = modules,
                            error = null,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            version = version.getOrThrow(),
                            error = error.message,
                        )
                    }
                }
        }
    }

    suspend fun install(
        file: File,
        metadata: KpmFileMetadata,
        persistent: Boolean,
    ): Result<Unit> = repository.install(file, metadata, persistent)
        .onSuccess { refresh() }

    suspend fun uninstall(moduleId: String): Result<Unit> =
        repository.uninstall(moduleId).onSuccess { refresh() }

    suspend fun control(moduleId: String, args: String): Result<Int> =
        repository.control(moduleId, args)
}
