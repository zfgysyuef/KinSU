#ifndef __KSU_MIGRATE_H
#define __KSU_MIGRATE_H

#include <linux/types.h>

/* Conflict type flags */
#define KSU_CONFLICT_MODULES    (1U << 0)  /* Old manager module directories */
#define KSU_CONFLICT_INIT       (1U << 1)  /* Init scripts (post-fs-data.d, service.d, etc.) */
#define KSU_CONFLICT_SEPOLICY   (1U << 2)  /* SELinux policy rules */
#define KSU_CONFLICT_MOUNT      (1U << 3)  /* Overlay/residual mount points */
#define KSU_CONFLICT_BOOT       (1U << 4)  /* Boot image conflicts */

/* Manager source identifiers */
#define KSU_MIGRATE_SRC_MAGISK   1
#define KSU_MIGRATE_SRC_APATCH   2
#define KSU_MIGRATE_SRC_OLDKSU   3

/* Backup base path */
#define KSU_BACKUP_BASE "/data/adb/kinsu_backup"

/*
 * ksu_detect_conflicts - Scan for conflicts from other managers
 * @mask: bitmask of conflict types to detect
 * @buf: user buffer for result JSON
 * @buf_size: size of user buffer
 *
 * Returns 0 on success, negative errno on error.
 */
int ksu_detect_conflicts(u32 mask, char __user *buf, u32 buf_size);

/*
 * ksu_clean_conflicts - Clean detected conflicts
 * @mask: bitmask of conflict types to clean
 * @backup: whether to create backup before cleaning
 * @buf: user buffer for result JSON
 * @buf_size: size of user buffer
 *
 * Returns 0 on success, negative errno on error.
 */
int ksu_clean_conflicts(u32 mask, u8 backup, char __user *buf, u32 buf_size);

/*
 * ksu_migrate_from_manager - Migrate modules from another manager
 * @source: manager source ID (KSU_MIGRATE_SRC_*)
 * @preserve_data: whether to preserve module data
 * @buf: user buffer for result JSON
 * @buf_size: size of user buffer
 *
 * Returns 0 on success, negative errno on error.
 */
int ksu_migrate_from_manager(u32 source, u8 preserve_data,
			     char __user *buf, u32 buf_size);

#endif /* __KSU_MIGRATE_H */
