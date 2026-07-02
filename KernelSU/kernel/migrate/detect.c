// SPDX-License-Identifier: GPL-2.0-only
/*
 * KinSU - Manager migration conflict detection
 *
 * Detects residual files from other root managers (Magisk, APatch, old KernelSU)
 * that may conflict with KinSU operation.
 */

#include <linux/fs.h>
#include <linux/namei.h>
#include <linux/slab.h>
#include <linux/string.h>
#include <linux/uaccess.h>
#include <linux/version.h>

#include "migrate.h"
#include "klog.h"

/* Paths to check for each conflict type */
static const char *magisk_module_paths[] = {
	"/data/adb/modules",
	NULL,
};

static const char *apatch_module_paths[] = {
	"/data/adb/ap",
	NULL,
};

static const char *oldksu_module_paths[] = {
	"/data/adb/ksu/modules",
	NULL,
};

static const char *init_script_paths[] = {
	"/data/adb/post-fs-data.d",
	"/data/adb/service.d",
	"/data/adb/post-mount.d",
	NULL,
};

static const char *sepolicy_paths[] = {
	"/data/adb/sepolicy.rules",
	NULL,
};

struct conflict_entry {
	const char *path;
	const char *source; /* "magisk", "apatch", "oldksu", "system" */
	const char *type;   /* "modules", "init", "sepolicy", "mount" */
};

/*
 * Check if a path exists in the filesystem.
 * Returns true if the path exists, false otherwise.
 */
static bool path_exists(const char *path)
{
	struct path p;
	int err;

	err = kern_path(path, LOOKUP_FOLLOW, &p);
	if (err)
		return false;

	path_put(&p);
	return true;
}

/*
 * Append a JSON conflict entry to the buffer.
 * Returns bytes written or negative error.
 */
static int append_conflict_json(char *buf, int offset, int buf_size,
				const char *path, const char *source,
				const char *type)
{
	int written;

	written = snprintf(buf + offset, buf_size - offset,
			   "%s{\"path\":\"%s\",\"source\":\"%s\",\"type\":\"%s\"}",
			   offset > 1 ? "," : "", path, source, type);

	if (written >= buf_size - offset)
		return -ENOSPC;

	return written;
}

/*
 * Scan for module directory conflicts.
 */
static int detect_module_conflicts(char *buf, int *offset, int buf_size)
{
	int ret = 0;
	int w;

	/* Check Magisk modules */
	if (path_exists(magisk_module_paths[0])) {
		w = append_conflict_json(buf, *offset, buf_size,
					 magisk_module_paths[0], "magisk", "modules");
		if (w < 0)
			return w;
		*offset += w;
	}

	/* Check APatch modules */
	if (path_exists(apatch_module_paths[0])) {
		w = append_conflict_json(buf, *offset, buf_size,
					 apatch_module_paths[0], "apatch", "modules");
		if (w < 0)
			return w;
		*offset += w;
	}

	/* Check old KernelSU modules */
	if (path_exists(oldksu_module_paths[0])) {
		w = append_conflict_json(buf, *offset, buf_size,
					 oldksu_module_paths[0], "oldksu", "modules");
		if (w < 0)
			return w;
		*offset += w;
	}

	return ret;
}

/*
 * Scan for init script conflicts.
 */
static int detect_init_conflicts(char *buf, int *offset, int buf_size)
{
	int i;

	for (i = 0; init_script_paths[i]; i++) {
		if (path_exists(init_script_paths[i])) {
			int w = append_conflict_json(buf, *offset, buf_size,
						     init_script_paths[i],
						     "system", "init");
			if (w < 0)
				return w;
			*offset += w;
		}
	}

	return 0;
}

/*
 * Scan for SELinux policy conflicts.
 */
static int detect_sepolicy_conflicts(char *buf, int *offset, int buf_size)
{
	int i;

	for (i = 0; sepolicy_paths[i]; i++) {
		if (path_exists(sepolicy_paths[i])) {
			int w = append_conflict_json(buf, *offset, buf_size,
						     sepolicy_paths[i],
						     "system", "sepolicy");
			if (w < 0)
				return w;
			*offset += w;
		}
	}

	return 0;
}

int ksu_detect_conflicts(u32 mask, char __user *buf, u32 buf_size)
{
	char *kbuf;
	int offset = 0;
	int ret = 0;

	if (!buf || buf_size < 3)
		return -EINVAL;

	kbuf = kzalloc(buf_size, GFP_KERNEL);
	if (!kbuf)
		return -ENOMEM;

	/* Start JSON array */
	offset = snprintf(kbuf, buf_size, "[");

	if (mask & KSU_CONFLICT_MODULES) {
		ret = detect_module_conflicts(kbuf, &offset, buf_size);
		if (ret)
			goto out;
	}

	if (mask & KSU_CONFLICT_INIT) {
		ret = detect_init_conflicts(kbuf, &offset, buf_size);
		if (ret)
			goto out;
	}

	if (mask & KSU_CONFLICT_SEPOLICY) {
		ret = detect_sepolicy_conflicts(kbuf, &offset, buf_size);
		if (ret)
			goto out;
	}

	/* Close JSON array */
	if (offset + 2 < buf_size) {
		kbuf[offset] = ']';
		kbuf[offset + 1] = '\0';
	} else {
		ret = -ENOSPC;
		goto out;
	}

	if (copy_to_user(buf, kbuf, offset + 2))
		ret = -EFAULT;

out:
	kfree(kbuf);
	return ret;
}
