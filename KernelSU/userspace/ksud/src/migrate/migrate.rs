//! Manager migration logic - migrating modules from other managers to KinSU.

use anyhow::{Context, Result};
use serde::{Deserialize, Serialize};
use std::fs;
use std::path::{Path, PathBuf};

use crate::defs;
use super::backup::BackupManager;
use super::clean::{ConflictCleaner, FsConflictCleaner, CleanResult};
use super::detect::CONFLICT_ALL;

/// Manager source identifiers
pub const SRC_MAGISK: u32 = 1;
pub const SRC_APATCH: u32 = 2;
pub const SRC_OLDKSU: u32 = 3;

/// Result of a migration operation.
#[derive(Debug, Serialize, Deserialize)]
pub struct MigrateResult {
    /// Whether migration succeeded overall
    pub success: bool,
    /// Source manager name
    pub source: String,
    /// Number of modules detected
    pub detected_count: u32,
    /// Number of modules successfully migrated
    pub migrated_count: u32,
    /// Number of modules skipped (incompatible)
    pub skipped_count: u32,
    /// Number of modules that failed migration
    pub failed_count: u32,
    /// Clean result if conflicts were cleaned
    pub clean_result: Option<CleanResult>,
    /// Details of each module migration
    pub details: Vec<ModuleMigrateDetail>,
    /// Errors encountered
    pub errors: Vec<String>,
}

/// Detail of a single module migration.
#[derive(Debug, Serialize, Deserialize)]
pub struct ModuleMigrateDetail {
    pub module_id: String,
    pub source_path: String,
    pub dest_path: Option<String>,
    pub status: String,  // "migrated", "skipped", "failed"
    pub reason: Option<String>,
}

/// Trait for migrating managers.
pub trait ManagerMigrator {
    /// Migrate modules from the specified source manager.
    fn migrate_from(&self, source: u32, preserve_data: bool) -> Result<MigrateResult>;

    /// Check if a module is compatible with KinSU.
    fn check_compatibility(&self, module_path: &Path) -> Result<bool>;
}

/// Default manager migrator.
pub struct FsManagerMigrator {
    backup_manager: BackupManager,
    kinsu_module_dir: PathBuf,
}

impl FsManagerMigrator {
    pub fn new() -> Self {
        Self {
            backup_manager: BackupManager::new(),
            kinsu_module_dir: PathBuf::from(defs::MODULE_DIR),
        }
    }

    fn get_source_dir(&self, source: u32) -> Result<&'static str> {
        match source {
            SRC_MAGISK => Ok("/data/adb/modules"),
            SRC_APATCH => Ok("/data/adb/ap"),
            SRC_OLDKSU => Ok("/data/adb/ksu/modules"),
            _ => anyhow::bail!("Unknown migration source: {}", source),
        }
    }

    fn get_source_name(&self, source: u32) -> &'static str {
        match source {
            SRC_MAGISK => "magisk",
            SRC_APATCH => "apatch",
            SRC_OLDKSU => "oldksu",
            _ => "unknown",
        }
    }

    fn scan_modules(&self, source_dir: &str) -> Result<Vec<String>> {
        let dir = Path::new(source_dir);
        if !dir.exists() {
            return Ok(Vec::new());
        }

        let modules: Vec<String> = fs::read_dir(dir)
            .with_context(|| format!("Failed to read module dir: {}", source_dir))?
            .filter_map(|e| e.ok())
            .filter(|e| e.path().is_dir())
            .filter(|e| {
                // Skip disabled modules (they have a "disable" file)
                !e.path().join("disable").exists()
                // Skip modules marked for removal
                && !e.path().join("remove").exists()
                // Skip update directories
                && !e.path().join("update").exists()
            })
            .map(|e| e.file_name().to_string_lossy().to_string())
            .collect();

        Ok(modules)
    }
}

impl ManagerMigrator for FsManagerMigrator {
    fn migrate_from(&self, source: u32, preserve_data: bool) -> Result<MigrateResult> {
        let source_dir = self.get_source_dir(source)?;
        let source_name = self.get_source_name(source);
        let modules = self.scan_modules(source_dir)?;
        let source_is_shared_module_dir =
            source_dir.trim_end_matches('/') == defs::MODULE_DIR.trim_end_matches('/');

        // Create KinSU module directory
        fs::create_dir_all(&self.kinsu_module_dir)
            .context("Failed to create KinSU module directory")?;

        let backup_dir = self.backup_manager.create_backup_dir()?;

        let mut result = MigrateResult {
            success: true,
            source: source_name.to_string(),
            detected_count: modules.len() as u32,
            migrated_count: 0,
            skipped_count: 0,
            failed_count: 0,
            clean_result: None,
            details: Vec::new(),
            errors: Vec::new(),
        };

        if source_is_shared_module_dir {
            for module_id in &modules {
                let module_path = Path::new(source_dir).join(module_id);
                result.migrated_count += 1;
                result.details.push(ModuleMigrateDetail {
                    module_id: module_id.clone(),
                    source_path: module_path.to_string_lossy().to_string(),
                    dest_path: Some(module_path.to_string_lossy().to_string()),
                    status: "migrated".to_string(),
                    reason: Some("Shared module directory; no copy required".to_string()),
                });
            }
            return Ok(result);
        }

        for module_id in &modules {
            let module_path = Path::new(source_dir).join(module_id);

            // Check compatibility
            match self.check_compatibility(&module_path) {
                Ok(true) => {}
                Ok(false) => {
                    result.skipped_count += 1;
                    result.details.push(ModuleMigrateDetail {
                        module_id: module_id.clone(),
                        source_path: module_path.to_string_lossy().to_string(),
                        dest_path: None,
                        status: "skipped".to_string(),
                        reason: Some("Module not compatible with KinSU".to_string()),
                    });
                    continue;
                }
                Err(e) => {
                    result.failed_count += 1;
                    result.details.push(ModuleMigrateDetail {
                        module_id: module_id.clone(),
                        source_path: module_path.to_string_lossy().to_string(),
                        dest_path: None,
                        status: "failed".to_string(),
                        reason: Some(format!("Compatibility check failed: {}", e)),
                    });
                    continue;
                }
            }

            // Backup original
            if let Err(e) = self.backup_manager.backup_path(&backup_dir, &module_path) {
                result.errors.push(format!(
                    "Backup failed for {}: {}", module_id, e
                ));
            }

            // Copy module to KinSU directory
            let dest_path = self.kinsu_module_dir.join(module_id);

            match copy_module(&module_path, &dest_path, preserve_data) {
                Ok(()) => {
                    result.migrated_count += 1;
                    result.details.push(ModuleMigrateDetail {
                        module_id: module_id.clone(),
                        source_path: module_path.to_string_lossy().to_string(),
                        dest_path: Some(dest_path.to_string_lossy().to_string()),
                        status: "migrated".to_string(),
                        reason: None,
                    });
                }
                Err(e) => {
                    result.failed_count += 1;
                    result.details.push(ModuleMigrateDetail {
                        module_id: module_id.clone(),
                        source_path: module_path.to_string_lossy().to_string(),
                        dest_path: Some(dest_path.to_string_lossy().to_string()),
                        status: "failed".to_string(),
                        reason: Some(format!("{}", e)),
                    });
                }
            }
        }

        // Clean old manager conflicts
        if result.migrated_count > 0 || result.skipped_count > 0 {
            let cleaner = FsConflictCleaner::new();
            match cleaner.clean_conflicts(CONFLICT_ALL, true) {
                Ok(clean) => result.clean_result = Some(clean),
                Err(e) => result.errors.push(format!("Post-migration clean failed: {}", e)),
            }
        }

        result.success = result.failed_count == 0;
        Ok(result)
    }

    fn check_compatibility(&self, module_path: &Path) -> Result<bool> {
        // Check for module.prop which is required by all module managers
        let prop_file = module_path.join("module.prop");
        if !prop_file.exists() {
            return Ok(false);
        }

        // Check for minimum required fields in module.prop
        let content = fs::read_to_string(&prop_file)
            .context("Failed to read module.prop")?;

        let has_id = content.lines().any(|l| l.starts_with("id="));
        let has_name = content.lines().any(|l| l.starts_with("name="));
        // Module must have at least id and name
        Ok(has_id && has_name)
    }
}

/// Copy a module directory, optionally preserving data files.
fn copy_module(src: &Path, dst: &Path, preserve_data: bool) -> Result<()> {
    fs::create_dir_all(dst)?;

    for entry in fs::read_dir(src)? {
        let entry = entry?;
        let file_name = entry.file_name();
        let name_str = file_name.to_string_lossy();

        // Skip certain files that shouldn't be migrated
        if name_str == "disable" || name_str == "remove" || name_str == "update" {
            continue;
        }

        // Optionally skip data directories
        if !preserve_data && (name_str == "data" || name_str == "system") {
            continue;
        }

        let dest = dst.join(&file_name);
        let ty = entry.file_type()?;

        if ty.is_dir() {
            copy_module(&entry.path(), &dest, preserve_data)?;
        } else if ty.is_file() {
            fs::copy(entry.path(), &dest)?;
        }
    }

    Ok(())
}
