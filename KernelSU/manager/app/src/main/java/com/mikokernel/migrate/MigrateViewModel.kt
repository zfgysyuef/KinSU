package com.mikokernel.migrate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the migration screen.
 */
data class MigrateUiState(
    val isLoading: Boolean = false,
    val conflicts: List<ConflictItem> = emptyList(),
    val hasDetectedConflicts: Boolean = false,
    val cleanResult: CleanResult? = null,
    val migrateResult: MigrateResult? = null,
    val selectedSource: MigrationSource? = null,
    val preserveData: Boolean = false,
    val error: String? = null,
    val step: MigrateStep = MigrateStep.SELECT_ACTION
)

enum class MigrateStep {
    SELECT_ACTION,     // Choose: detect conflicts or migrate
    DETECTING,         // Scanning for conflicts
    CONFLICTS_FOUND,   // Show detected conflicts, offer clean
    CLEANING,          // Cleaning in progress
    CLEAN_RESULT,      // Show clean result
    SELECT_SOURCE,     // Choose migration source
    MIGRATING,         // Migration in progress
    MIGRATE_RESULT     // Show migration result
}

/**
 * ViewModel for the migration screen.
 */
class MigrateViewModel(
    private val repository: MigrateRepository = MigrateRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MigrateUiState())
    val uiState: StateFlow<MigrateUiState> = _uiState.asStateFlow()

    /**
     * Detect conflicts from other root managers.
     */
    fun detectConflicts() {
        _uiState.update { it.copy(isLoading = true, step = MigrateStep.DETECTING, error = null) }

        viewModelScope.launch {
            try {
                val conflicts = repository.detectConflicts()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        conflicts = conflicts,
                        hasDetectedConflicts = conflicts.isNotEmpty(),
                        step = MigrateStep.CONFLICTS_FOUND
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Detection failed",
                        step = MigrateStep.SELECT_ACTION
                    )
                }
            }
        }
    }

    /**
     * Clean detected conflicts.
     */
    fun cleanConflicts(backup: Boolean = true) {
        _uiState.update { it.copy(isLoading = true, step = MigrateStep.CLEANING, error = null) }

        viewModelScope.launch {
            try {
                val result = repository.cleanConflicts(backup = backup)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        cleanResult = result,
                        step = MigrateStep.CLEAN_RESULT
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Clean failed",
                        step = MigrateStep.CONFLICTS_FOUND
                    )
                }
            }
        }
    }

    /**
     * Select a migration source manager.
     */
    fun selectSource(source: MigrationSource) {
        _uiState.update { it.copy(selectedSource = source) }
    }

    /**
     * Set whether to preserve module data during migration.
     */
    fun setPreserveData(preserve: Boolean) {
        _uiState.update { it.copy(preserveData = preserve) }
    }

    /**
     * Start migration from the selected source.
     */
    fun startMigration() {
        val source = _uiState.value.selectedSource ?: return
        _uiState.update { it.copy(isLoading = true, step = MigrateStep.MIGRATING, error = null) }

        viewModelScope.launch {
            try {
                val result = repository.migrateFrom(source, _uiState.value.preserveData)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        migrateResult = result,
                        step = MigrateStep.MIGRATE_RESULT
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Migration failed",
                        step = MigrateStep.SELECT_SOURCE
                    )
                }
            }
        }
    }

    /**
     * Navigate to the migration source selection step.
     */
    fun goToSelectSource() {
        _uiState.update { it.copy(step = MigrateStep.SELECT_SOURCE, error = null) }
    }

    /**
     * Navigate back to the action selection step.
     */
    fun goToSelectAction() {
        _uiState.update {
            MigrateUiState() // Reset all state
        }
    }

    /**
     * Clear any error message.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
