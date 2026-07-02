//! Manager migration conflict detection, cleaning, and module migration.
//!
//! Handles transitioning from other root managers (Magisk, APatch, old KernelSU)
//! to KinSU by detecting residual conflicts, safely cleaning them, and migrating
//! compatible modules.

pub mod backup;
pub mod clean;
pub mod detect;
pub mod migrate;

pub use backup::BackupManager;
pub use clean::{CleanResult, ConflictCleaner};
pub use detect::{ConflictDetector, ConflictItem};
pub use migrate::{MigrateResult, ManagerMigrator};
