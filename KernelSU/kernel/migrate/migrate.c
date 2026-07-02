// SPDX-License-Identifier: GPL-2.0-only
/*
 * KinSU - Manager migration logic
 *
 * Handles migration from other root managers (Magisk, APatch, old KernelSU)
 * to KinSU. Generates migration plans for ksud to execute.
 */

#include <linux/fs.h>
#include <linux/namei.h>
#include <linux/slab.h>
#include <linux/string.h>
#include <linux/uaccess.h>

#include "migrate.h"
#include "klog.h"

/* Migration source directories */
static const char *magisk_module_dir = "/data/adb/modules";
static const char *apatch_module_dir = "/data/adb/ap";
static const char *oldksu_module_dir = "/data/adb/ksu/modules";

static const char *get_source_dir(u32 source)
{
	switch (source) {
	case KSU_MIGRATE_SRC_MAGISK:
		return magisk_module_dir;
	case KSU_MIGRATE_SRC_APATCH:
		return apatch_module_dir;
	case KSU_MIGRATE_SRC_OLDKSU:
		return oldksu_module_dir;
	default:
		return NULL;
	}
}

static const char *get_source_name(u32 source)
{
	switch (source) {
	case KSU_MIGRATE_SRC_MAGISK:
		return "magisk";
	case KSU_MIGRATE_SRC_APATCH:
		return "apatch";
	case KSU_MIGRATE_SRC_OLDKSU:
		return "oldksu";
	default:
		return "unknown";
	}
}

int ksu_migrate_from_manager(u32 source, u8 preserve_data,
			     char __user *buf, u32 buf_size)
{
	const char *source_dir;
	const char *source_name;
	char *kbuf;
	int offset;
	int ret = 0;

	source_dir = get_source_dir(source);
	source_name = get_source_name(source);

	if (!source_dir)
		return -EINVAL;

	if (!buf || buf_size < 3)
		return -EINVAL;

	kbuf = kzalloc(buf_size, GFP_KERNEL);
	if (!kbuf)
		return -ENOMEM;

	/*
	 * Generate a migration plan as JSON for ksud to execute.
	 * The kernel module only validates permissions and generates the plan;
	 * all filesystem operations happen in userspace.
	 */
	offset = snprintf(kbuf, buf_size,
			  "{\"source\":\"%s\","
			  "\"source_dir\":\"%s\","
			  "\"preserve_data\":%s,"
			  "\"kinSU_dir\":\"/data/adb/kinsu/modules\","
			  "\"actions\":[",
			  source_name, source_dir,
			  preserve_data ? "true" : "false");

	/*
	 * The actual module scanning and compatibility checking is done by
	 * ksud in userspace. The kernel just provides the plan structure
	 * and validates that the caller has root privileges.
	 */
	offset += snprintf(kbuf + offset, buf_size - offset,
			   "{\"type\":\"scan\",\"path\":\"%s\"}", source_dir);

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
