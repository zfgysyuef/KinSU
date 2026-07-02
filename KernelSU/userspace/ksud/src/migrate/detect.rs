//! Conflict detection for other root manager residuals.

use anyhow::{Context, Result};
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
            detect_init_conflicts(&mut conflicts)?;
        }

        if mask & CONFLICT_SEPOLICY != 0 {
            detect_sepolicy_conflicts(&mut conflicts)?;
        }

        Ok(conflicts)
    }
}

fn detect_module_conflicts(conflicts: &mut Vec<ConflictItem>) -> Result<()> {
    let paths = [
        ("/data/adb/modules", "magisk", "modules"),
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

fn detect_init_conflicts(conflicts: &mut Vec<ConflictItem>) -> Result<()> {
    let dirs = [
        "/data/adb/post-fs-data.d",
        "/data/adb/service.d",
        "/data/adb/post-mount.d",
    ];

    for dir in &dirs {
        if Path::new(dir).exists() {
            let has_scripts = std::fs::read_dir(dir)
                .map(|mut d| {
                    d.any(|e| {
                        e.ok()
                            .map(|e| e.path().is_file())
                            .unwrap_or(false)
                    })
                })
                .unwrap_or(false);

            if has_scripts {
                conflicts.push(ConflictItem {
                    path: dir.to_string(),
                    source: "system".to_string(),
                    conflict_type: "init".to_string(),
                });
            }
        }
    }

    Ok(())
}

fn detect_sepolicy_conflicts(conflicts: &mut Vec<ConflictItem>) -> Result<()> {
    let path = "/data/adb/sepolicy.rules";
    if Path::new(path).exists() {
        conflicts.push(ConflictItem {
            path: path.to_string(),
            source: "system".to_string(),
            conflict_type: "sepolicy".to_string(),
        });
    }
    Ok(())
}

/// Detect conflicts via kernel IOCTL (if kernel module is loaded).
/// Falls back to filesystem detection if IOCTL is unavailable.
pub fn detect_conflicts_ioctl(mask: u32) -> Result<Vec<ConflictItem>> {
    use crate::ksu_uapi;
    use crate::ksucalls::ksuctl;

    // Try IOCTL first
    let mut buf = vec![0u8; 8192];
    let mut cmd = ksu_uapi::ksu_detect_conflicts_cmd {
        mask,
        buf: buf.as_mut_ptr() as u64,
        buf_size: buf.len() as u32,
        result: 0,
    };

    match ksuctl(ksu_uapi::KSU_IOCTL_DETECT_CONFLICTS, &raw mut cmd) {
        Ok(_) if cmd.result == 0 => {
            let json_str = std::ffi::CStr::from_bytes_until_nul(&buf)
                .ok()
                .and_then(|s| s.to_str().ok())
                .unwrap_or("[]");
            serde_json::from_str::<Vec<ConflictItem>>(json_str)
                .context("Failed to parse conflict detection result")
        }
        _ => {
            // Fallback to filesystem detection
            let detector = FsConflictDetector;
            detector.detect_conflicts(mask)
        }
    }
}
