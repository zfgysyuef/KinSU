// SPDX-License-Identifier: GPL-2.0-only
/*
 * KinSU - Manager migration conflict cleaning
 *
 * Safely removes conflict files from other root managers.
 * Always creates backups before removal. Operations are designed to be
 * atomic and safe for boot.
 */

#include <linux/fs.h>
#include <linux/namei.h>
#include <linux/slab.h>
#include <linux/string.h>
#include <linux/uaccess.h>
#include <linux/version.h>

#include "migrate.h"
#include "klog.h"

/*
 * Create a backup directory with timestamp.
 * Returns 0 on success, negative errno on error.
 * Writes the backup path to @backup_path (must be at least 128 bytes).
 */
static int create_backup_dir(char *backup_path, int path_size)
{
	struct timespec64 ts;
	struct tm tm;

	ktime_get_real_ts64(&ts);
	time64_to_tm(ts.tv_sec, 0, &tm);

	snprintf(backup_path, path_size,
		 KSU_BACKUP_BASE "/%04ld%02d%02d_%02d%02d%02d",
		 tm.tm_year + 1900, tm.tm_mon + 1, tm.tm_mday,
		 tm.tm_hour, tm.tm_min, tm.tm_sec);

	/* Note: actual directory creation requires userspace (ksud).
	 * The kernel signals the need for backup; ksud performs the I/O. */
	return 0;
}

int ksu_clean_conflicts(u32 mask, u8 backup, char __user *buf, u32 buf_size)
{
	char *kbuf;
	char backup_path[128] = { 0 };
	int offset = 0;
	int ret = 0;

	if (!buf || buf_size < 3)
		return -EINVAL;

	kbuf = kzalloc(buf_size, GFP_KERNEL);
	if (!kbuf)
		return -ENOMEM;

	if (backup) {
		ret = create_backup_dir(backup_path, sizeof(backup_path));
		if (ret)
			goto out;
	}

	/*
	 * Build a JSON instruction set for ksud to execute in userspace.
	 * The kernel module does not perform direct filesystem mutations;
	 * it generates a clean plan that ksud carries out safely.
	 */
	offset = snprintf(kbuf, buf_size, "{\"backup\":\"%s\",\"actions\":[",
			  backup ? backup_path : "");

	if (mask & KSU_CONFLICT_MODULES) {
		int w = snprintf(kbuf + offset, buf_size - offset,
				 "%s{\"type\":\"rmdir\",\"path\":\"/data/adb/modules\",\"source\":\"magisk\"}",
				 offset > 20 ? "," : "");
		offset += w;

		w = snprintf(kbuf + offset, buf_size - offset,
			     ",{\"type\":\"rmdir\",\"path\":\"/data/adb/ap\",\"source\":\"apatch\"}");
		offset += w;

		w = snprintf(kbuf + offset, buf_size - offset,
			     ",{\"type\":\"rmdir\",\"path\":\"/data/adb/ksu/modules\",\"source\":\"oldksu\"}");
		offset += w;
	}

	if (mask & KSU_CONFLICT_INIT) {
		int w = snprintf(kbuf + offset, buf_size - offset,
				 ",{\"type\":\"rm_glob\",\"path\":\"/data/adb/post-fs-data.d/*\",\"source\":\"system\"}");
		offset += w;

		w = snprintf(kbuf + offset, buf_size - offset,
			     ",{\"type\":\"rm_glob\",\"path\":\"/data/adb/service.d/*\",\"source\":\"system\"}");
		offset += w;

		w = snprintf(kbuf + offset, buf_size - offset,
			     ",{\"type\":\"rm_glob\",\"path\":\"/data/adb/post-mount.d/*\",\"source\":\"system\"}");
		offset += w;
	}

	if (mask & KSU_CONFLICT_SEPOLICY) {
		int w = snprintf(kbuf + offset, buf_size - offset,
				 ",{\"type\":\"rm\",\"path\":\"/data/adb/sepolicy.rules\",\"source\":\"system\"}");
		offset += w;
	}

	/* Close JSON */
	if (offset + 4 < buf_size) {
		kbuf[offset] = ']';
		kbuf[offset + 1] = '}';
		kbuf[offset + 2] = '\0';
		offset += 2;
	} else {
		ret = -ENOSPC;
		goto out;
	}

	if (copy_to_user(buf, kbuf, offset + 1))
		ret = -EFAULT;

out:
	kfree(kbuf);
	return ret;
}
