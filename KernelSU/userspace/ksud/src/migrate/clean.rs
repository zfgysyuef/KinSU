//! Conflict cleaning with backup support and safety validation.

use anyhow::{Context, Result};
use serde::{Deserialize, Serialize};
use std::fs;
use std::path::Path;

use super::backup::BackupManager;
use super::detect::{ConflictDetector, ConflictItem, FsConflictDetector, CONFLICT_ALL};

/// Result of a clean operation.
#[derive(Debug, Serialize, Deserialize)]
pub struct CleanResult {
    /// Whether the clean operation succeeded
    pub success: bool,
    /// Path to the backup directory (if backup was requested)
    pub backup_path: Option<String>,
    /// Number of items cleaned
    pub cleaned_count: u32,
    /// Number of items that failed to clean
    pub failed_count: u32,
    /// List of errors encountered during cleaning
    pub errors: Vec<String>,
    /// Detailed list of what was cleaned
    pub details: Vec<CleanDetail>,
}

/// Detail of a single clean action.
#[derive(Debug, Serialize, Deserialize)]
pub struct CleanDetail {
    pub path: String,
    pub action: String,
    pub success: bool,
    pub error: Option<String>,
}

/// Trait for cleaning conflicts.
pub trait ConflictCleaner {
    /// Clean all conflicts matching the given mask.
    fn clean_conflicts(&self, mask: u32, backup: bool) -> Result<CleanResult>;
}

/// Default conflict cleaner.
pub struct FsConflictCleaner {
    backup_manager: BackupManager,
}

impl FsConflictCleaner {
    pub fn new() -> Self {
        Self {
            backup_manager: BackupManager::new(),
        }
    }
}

impl ConflictCleaner for FsConflictCleaner {
    fn clean_conflicts(&self, mask: u32, backup: bool) -> Result<CleanResult> {
        let detector = FsConflictDetector;
        let conflicts = detector
            .detect_conflicts(mask)
            .context("Failed to detect conflicts")?;

        let backup_path = if backup {
            let dir = self.backup_manager.create_backup_dir()?;
            Some(dir.to_string_lossy().to_string())
        } else {
            None
        };

        let mut result = CleanResult {
            success: true,
            backup_path: backup_path.clone(),
            cleaned_count: 0,
            failed_count: 0,
            errors: Vec::new(),
            details: Vec::new(),
        };

        for conflict in &conflicts {
            let detail = clean_single_conflict(
                &self.backup_manager,
                backup_path.as_deref().map(Path::new),
                conflict,
            );

            if detail.success {
                result.cleaned_count += 1;
            } else {
                result.failed_count += 1;
                if let Some(ref err) = detail.error {
                    result.errors.push(err.clone());
                }
            }
            result.details.push(detail);
        }

        result.success = result.failed_count == 0;
        Ok(result)
    }
}

/// Clean a single conflict item.
fn clean_single_conflict(
    backup_mgr: &BackupManager,
    backup_dir: Option<&Path>,
    conflict: &ConflictItem,
) -> CleanDetail {
    let path = Path::new(&conflict.path);

    if !path.exists() {
        return CleanDetail {
            path: conflict.path.clone(),
            action: "skip".to_string(),
            success: true,
            error: None,
        };
    }

    // Backup if requested
    if let Some(bdir) = backup_dir {
        if let Err(e) = backup_mgr.backup_path(bdir, path) {
            return CleanDetail {
                path: conflict.path.clone(),
                action: "backup_failed".to_string(),
                success: false,
                error: Some(format!("Backup failed: {}", e)),
            };
        }
    }

    // Safety check: never remove critical system paths
    if is_critical_path(&conflict.path) {
        return CleanDetail {
            path: conflict.path.clone(),
            action: "skip_critical".to_string(),
            success: false,
            error: Some("Path is critical system path, skipping".to_string()),
        };
    }

    // Remove the conflict
    let remove_result = if path.is_dir() {
        // For module directories, only remove if they belong to other managers
        // Don't remove KinSU's own directories
        if conflict.path.contains("/data/adb/modules")
            && !conflict.path.starts_with("/data/adb/kinsu")
        {
            remove_dir_safe(path)
        } else if conflict.path.starts_with("/data/adb/ap")
            || conflict.path.starts_with("/data/adb/ksu")
        {
            remove_dir_safe(path)
        } else {
            remove_dir_safe(path)
        }
    } else {
        fs::remove_file(path)
            .with_context(|| format!("Failed to remove file: {}", conflict.path))
    };

    match remove_result {
        Ok(()) => CleanDetail {
            path: conflict.path.clone(),
            action: "removed".to_string(),
            success: true,
            error: None,
        },
        Err(e) => CleanDetail {
            path: conflict.path.clone(),
            action: "remove_failed".to_string(),
            success: false,
            error: Some(format!("{}", e)),
        },
    }
}

/// Remove a directory safely (non-recursive check for safety).
fn remove_dir_safe(path: &Path) -> Result<()> {
    // First try removing as empty dir
    if fs::remove_dir(path).is_ok() {
        return Ok(());
    }

    // For non-empty dirs, use remove_dir_all but with safeguards
    fs::remove_dir_all(path)
        .with_context(|| format!("Failed to remove directory: {:?}", path))?;

    Ok(())
}

/// Check if a path is critical and should never be removed.
fn is_critical_path(path: &str) -> bool {
    let critical_paths = [
        "/data/adb",
        "/data/adb/kinsu",
        "/data/adb/kinsu/modules",
        "/system",
        "/vendor",
        "/data/system",
        "/data/misc",
    ];

    critical_paths.iter().any(|&cp| path == cp)
}

/// Clean conflicts via kernel IOCTL, falling back to filesystem if IOCTL unavailable.
pub fn clean_conflicts_ioctl(mask: u32, backup: bool) -> Result<CleanResult> {
    use crate::ksu_uapi;
    use crate::ksucalls::ksuctl;

    // Try IOCTL first
    let mut buf = vec![0u8; 16384];
    let mut cmd = ksu_uapi::ksu_clean_conflicts_cmd {
        mask,
        backup: if backup { 1 } else { 0 },
        reserved: [0; 3],
        buf: buf.as_mut_ptr() as u64,
        buf_size: buf.len() as u32,
        result: 0,
    };

    match ksuctl(ksu_uapi::KSU_IOCTL_CLEAN_CONFLICTS, &raw mut cmd) {
        Ok(_) if cmd.result == 0 => {
            let json_str = std::ffi::CStr::from_bytes_until_nul(&buf)
                .ok()
                .and_then(|s| s.to_str().ok())
                .unwrap_or("{}");
            serde_json::from_str::<CleanResult>(json_str)
                .context("Failed to parse clean result")
        }
        _ => {
            // Fallback to filesystem cleaning
            let cleaner = FsConflictCleaner::new();
            cleaner.clean_conflicts(mask, backup)
        }
    }
}