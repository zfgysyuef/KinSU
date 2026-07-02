//! Backup management for migration operations.

use anyhow::{Context, Result};
use std::fs;
use std::path::{Path, PathBuf};
use std::time::{SystemTime, UNIX_EPOCH};

/// Manages backup creation and validation for migration operations.
pub struct BackupManager {
    base_dir: PathBuf,
}

impl BackupManager {
    pub fn new() -> Self {
        Self {
            base_dir: PathBuf::from("/data/adb/kinsu_backup"),
        }
    }

    pub fn with_base_dir(base_dir: PathBuf) -> Self {
        Self { base_dir }
    }

    /// Create a new timestamped backup directory.
    /// Returns the path to the created backup directory.
    pub fn create_backup_dir(&self) -> Result<PathBuf> {
        let timestamp = SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .context("System time error")?
            .as_secs();

        let backup_dir = self.base_dir.join(format!("{}", timestamp));
        fs::create_dir_all(&backup_dir)
            .with_context(|| format!("Failed to create backup dir: {:?}", backup_dir))?;

        Ok(backup_dir)
    }

    /// Backup a file or directory to the backup directory.
    /// Preserves the relative path structure.
    pub fn backup_path(&self, backup_dir: &Path, source: &Path) -> Result<PathBuf> {
        if !source.exists() {
            return Ok(backup_dir.to_path_buf());
        }

        let file_name = source
            .file_name()
            .context("Invalid source path")?;

        let dest = backup_dir.join(file_name);

        if source.is_dir() {
            copy_dir_recursive(source, &dest)?;
        } else {
            fs::copy(source, &dest)
                .with_context(|| format!("Failed to backup {:?} to {:?}", source, dest))?;
        }

        Ok(dest)
    }

    /// Validate that a backup directory contains expected content.
    pub fn validate_backup(&self, backup_dir: &Path) -> Result<bool> {
        if !backup_dir.exists() {
            return Ok(false);
        }

        let entries: Vec<_> = fs::read_dir(backup_dir)
            .context("Failed to read backup dir")?
            .filter_map(|e| e.ok())
            .collect();

        Ok(!entries.is_empty())
    }

    /// List all existing backups.
    pub fn list_backups(&self) -> Result<Vec<PathBuf>> {
        if !self.base_dir.exists() {
            return Ok(Vec::new());
        }

        let mut backups: Vec<PathBuf> = fs::read_dir(&self.base_dir)
            .context("Failed to read backup base dir")?
            .filter_map(|e| e.ok())
            .map(|e| e.path())
            .filter(|p| p.is_dir())
            .collect();

        backups.sort();
        Ok(backups)
    }
}

/// Recursively copy a directory.
fn copy_dir_recursive(src: &Path, dst: &Path) -> Result<()> {
    fs::create_dir_all(dst)?;

    for entry in fs::read_dir(src)? {
        let entry = entry?;
        let ty = entry.file_type()?;
        let dest_path = dst.join(entry.file_name());

        if ty.is_dir() {
            copy_dir_recursive(&entry.path(), &dest_path)?;
        } else if ty.is_file() {
            fs::copy(entry.path(), &dest_path)?;
        }
    }

    Ok(())
}
