//! Conflict detection for other root manager residuals.

use anyhow::Result;
use serde::{Deserialize, Serialize};
use std::path::Path;

/// Conflict type bitmask flags
pub const CONFLICT_MODULES: u32 = 1 << 0;
pub const CONFLICT_INIT: u32 = 1 << 1;
pub const CONFLICT_SEPOLICY: u32 = 1 << 2;
pub const CONFLICT_MOUNT: u32 = 1 << 3;
pub const CONFLICT_BOOT: u32 = 1 << 4;
pub const CONFLICT_ALL: u32 = 0x1F;

/// A single detected conflict item.
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ConflictItem {
    /// Filesystem path of the conflict
    pub path: String,
    /// Source manager: "magisk", "apatch", "oldksu", "system"
    pub source: String,
    /// Conflict type: "modules", "init", "sepolicy", "mount"
    #[serde(rename = "type")]
    pub conflict_type: String,
}

/// Trait for detecting conflicts from other root managers.
pub trait ConflictDetector {
    /// Scan for conflicts matching the given mask.
    fn detect_conflicts(&self, mask: u32) -> Result<Vec<ConflictItem>>;
}

/// Default conflict detector that checks filesystem paths.
pub struct FsConflictDetector;

impl ConflictDetector for FsConflictDetector {
    fn detect_conflicts(&self, mask: u32) -> Result<Vec<ConflictItem>> {
        let mut conflicts = Vec::new();

        if mask & CONFLICT_MODULES != 0 {
            detect_module_conflicts(&mut conflicts)?;
        }

        if mask & CONFLICT_INIT != 0 {
            detect_init_conflicts(&mut conflicts);
        }

        if mask & CONFLICT_SEPOLICY != 0 {
            detect_sepolicy_conflicts(&mut conflicts);
        }

        Ok(conflicts)
    }
}

fn detect_module_conflicts(conflicts: &mut Vec<ConflictItem>) -> Result<()> {
    let paths = [
        ("/data/adb/ap", "apatch", "modules"),
        ("/data/adb/ksu/modules", "oldksu", "modules"),
    ];

    for (path, source, ctype) in &paths {
        if Path::new(path).exists() {
            // Only report if directory has actual module content
            let has_content = std::fs::read_dir(path)
                .map(|mut d| d.any(|e| e.is_ok()))
                .unwrap_or(false);

            if has_content {
                conflicts.push(ConflictItem {
                    path: path.to_string(),
                    source: source.to_string(),
                    conflict_type: ctype.to_string(),
                });
            }
        }
    }

    Ok(())
}

fn detect_init_conflicts(_conflicts: &mut Vec<ConflictItem>) {
    // Shared init script directories are used by multiple root managers and modules.
    // Never report the whole directory as a conflict.
}

fn detect_sepolicy_conflicts(_conflicts: &mut Vec<ConflictItem>) {
    // /data/adb/sepolicy.rules is a shared compatibility file, not manager-owned state.
}

/// Detect conflicts from known non-shared manager-owned paths.
pub fn detect_conflicts_ioctl(mask: u32) -> Result<Vec<ConflictItem>> {
    let detector = FsConflictDetector;
    detector.detect_conflicts(mask)
}
