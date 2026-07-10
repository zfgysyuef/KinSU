/* SPDX-License-Identifier: GPL-2.0-or-later */
/*
 * KinSU compatibility bridge for the SukiSU KernelPatch KPM loader.
 * Copyright (C) 2025 Liankong (xhsw.new@outlook.com).
 */

#include <linux/kernel.h>
#include <linux/fs.h>
#include <linux/kernfs.h>
#include <linux/file.h>
#include <linux/vmalloc.h>
#include <linux/uaccess.h>
#include <linux/elf.h>
#include <linux/kallsyms.h>
#include <linux/version.h>
#include <linux/list.h>
#include <linux/spinlock.h>
#include <linux/rcupdate.h>
#include <asm/elf.h>
#include <linux/mm.h>
#include <linux/string.h>
#include <asm/cacheflush.h>
#include <linux/module.h>
#include <linux/set_memory.h>
#include <linux/export.h>
#include <linux/slab.h>
#include <asm/insn.h>
#include <linux/kprobes.h>
#include <linux/stacktrace.h>
#if LINUX_VERSION_CODE >= KERNEL_VERSION(5, 0, 0) && defined(CONFIG_MODULES)
#include <linux/moduleloader.h>
#endif
#include "kpm.h"
#include "compact.h"

#define KPM_NAME_LEN 32
#define KPM_ARGS_LEN 1024
#define KPM_PATH_LEN 256
#define KPM_INFO_LEN 256
#define KPM_LIST_LEN 4096
#define KPM_INTERNAL_INFO_LEN 4096
#define KPM_INTERNAL_LIST_LEN (1024 * 1024)

#ifndef NO_OPTIMIZE
#if defined(__GNUC__) && !defined(__clang__)
#define NO_OPTIMIZE __attribute__((optimize("O0")))
#elif defined(__clang__)
#define NO_OPTIMIZE __attribute__((optnone))
#else
#define NO_OPTIMIZE
#endif
#endif

noinline NO_OPTIMIZE void sukisu_kpm_load_module_path(const char *path,
                                                      const char *args,
                                                      void *ptr, int *result)
{
    pr_info("kpm: Stub function called (sukisu_kpm_load_module_path). "
            "path=%s args=%s ptr=%p\n",
            path, args, ptr);

    __asm__ volatile("nop");
}
EXPORT_SYMBOL(sukisu_kpm_load_module_path);

noinline NO_OPTIMIZE void sukisu_kpm_unload_module(const char *name, void *ptr,
                                                   int *result)
{
    pr_info("kpm: Stub function called (sukisu_kpm_unload_module). "
            "name=%s ptr=%p\n",
            name, ptr);

    __asm__ volatile("nop");
}
EXPORT_SYMBOL(sukisu_kpm_unload_module);

noinline NO_OPTIMIZE void sukisu_kpm_num(int *result)
{
    pr_info("kpm: Stub function called (sukisu_kpm_num).\n");

    __asm__ volatile("nop");
}
EXPORT_SYMBOL(sukisu_kpm_num);

noinline NO_OPTIMIZE void sukisu_kpm_info(const char *name, char *buf,
                                          int bufferSize, int *size)
{
    pr_info("kpm: Stub function called (sukisu_kpm_info). "
            "name=%s buffer=%p\n",
            name, buf);

    __asm__ volatile("nop");
}
EXPORT_SYMBOL(sukisu_kpm_info);

noinline NO_OPTIMIZE void sukisu_kpm_list(void *out, int bufferSize,
                                          int *result)
{
    pr_info("kpm: Stub function called (sukisu_kpm_list). "
            "buffer=%p size=%d\n",
            out, bufferSize);
}
EXPORT_SYMBOL(sukisu_kpm_list);

noinline NO_OPTIMIZE void sukisu_kpm_control(const char *name, const char *args,
                                             long arg_len, int *result)
{
    pr_info("kpm: Stub function called (sukisu_kpm_control). "
            "name=%p args=%p arg_len=%ld\n",
            name, args, arg_len);

    __asm__ volatile("nop");
}
EXPORT_SYMBOL(sukisu_kpm_control);

noinline NO_OPTIMIZE void sukisu_kpm_version(char *buf, int bufferSize)
{
    pr_info("kpm: Stub function called (sukisu_kpm_version). "
            "buffer=%p\n",
            buf);
}
EXPORT_SYMBOL(sukisu_kpm_version);

noinline int sukisu_handle_kpm(unsigned long control_code, unsigned long arg1,
                               unsigned long arg2, unsigned long result_code)
{
    int res = -ENOSYS;

    if (control_code == SUKISU_KPM_LOAD) {
        char kernel_load_path[KPM_PATH_LEN] = { 0 };
        char kernel_args_buffer[KPM_ARGS_LEN] = { 0 };
        long copied;

        if (!arg1 || !access_ok((void __user *)arg1, 1))
            goto invalid_arg;

        copied = strncpy_from_user(kernel_load_path,
                                   (const char __user *)arg1,
                                   sizeof(kernel_load_path));
        if (copied < 0)
            goto invalid_arg;
        if (copied >= sizeof(kernel_load_path)) {
            res = -ENAMETOOLONG;
            goto exit;
        }

        if (arg2) {
            if (!access_ok((void __user *)arg2, 1))
                goto invalid_arg;
            copied = strncpy_from_user(kernel_args_buffer,
                                       (const char __user *)arg2,
                                       sizeof(kernel_args_buffer));
            if (copied < 0)
                goto invalid_arg;
            if (copied >= sizeof(kernel_args_buffer)) {
                res = -E2BIG;
                goto exit;
            }
        }

        sukisu_kpm_load_module_path(kernel_load_path, kernel_args_buffer, NULL,
                                    &res);
    } else if (control_code == SUKISU_KPM_UNLOAD) {
        char kernel_name_buffer[KPM_NAME_LEN] = { 0 };
        long copied;

        if (!arg1 || !access_ok((void __user *)arg1, 1))
            goto invalid_arg;

        copied = strncpy_from_user(kernel_name_buffer,
                                   (const char __user *)arg1,
                                   sizeof(kernel_name_buffer));
        if (copied < 0)
            goto invalid_arg;
        if (copied >= sizeof(kernel_name_buffer)) {
            res = -ENAMETOOLONG;
            goto exit;
        }

        sukisu_kpm_unload_module(kernel_name_buffer, NULL, &res);
    } else if (control_code == SUKISU_KPM_NUM) {
        sukisu_kpm_num(&res);
    } else if (control_code == SUKISU_KPM_INFO) {
        char kernel_name_buffer[KPM_NAME_LEN] = { 0 };
        char *buf;
        int size = 0;
        long copied;
        size_t copy_len;

        if (!arg1 || !arg2 ||
            !access_ok((void __user *)arg1, 1) ||
            !access_ok((void __user *)arg2, KPM_INFO_LEN))
            goto invalid_arg;

        copied = strncpy_from_user(kernel_name_buffer,
                                   (const char __user *)arg1,
                                   sizeof(kernel_name_buffer));
        if (copied < 0)
            goto invalid_arg;
        if (copied >= sizeof(kernel_name_buffer)) {
            res = -ENAMETOOLONG;
            goto exit;
        }

        /*
         * KernelPatch 0.13.0 writes out_info[snprintf_return - 1].  Keep its
         * write target separate from the fixed 256-byte userspace ABI so a
         * long, but valid, description/argument string cannot corrupt this
         * stack frame before we get a chance to truncate it.
         */
        buf = kvzalloc(KPM_INTERNAL_INFO_LEN, GFP_KERNEL);
        if (!buf) {
            res = -ENOMEM;
            goto exit;
        }

        sukisu_kpm_info(kernel_name_buffer, buf,
                        KPM_INTERNAL_INFO_LEN - 1, &size);
        copy_len = strnlen(buf, KPM_INTERNAL_INFO_LEN);
        if (size < 0) {
            res = size;
        } else if (size >= KPM_INTERNAL_INFO_LEN ||
                   copy_len >= KPM_INTERNAL_INFO_LEN) {
            res = -EOVERFLOW;
        } else {
            copy_len = min_t(size_t, copy_len, KPM_INFO_LEN - 1);
            buf[copy_len] = '\0';
            res = copy_to_user((void __user *)arg2, buf, copy_len + 1) ?
                          -EFAULT :
                          0;
        }
        kvfree(buf);
    } else if (control_code == SUKISU_KPM_LIST) {
        char *buf;
        int len;
        size_t copy_len;

        if (!arg1 || arg2 <= 1 || arg2 > KPM_LIST_LEN)
            goto invalid_arg;
        len = (int)arg2;
        if (!access_ok((void __user *)arg1, len))
            goto invalid_arg;

        /*
         * Do not expose the caller's small buffer to KernelPatch 0.13.0's
         * truncation arithmetic.  It accumulates snprintf's would-have-been
         * length and can otherwise continue with a negative remaining size.
         */
        buf = kvzalloc(KPM_INTERNAL_LIST_LEN, GFP_KERNEL);
        if (!buf) {
            res = -ENOMEM;
            goto exit;
        }

        sukisu_kpm_list(buf, KPM_INTERNAL_LIST_LEN - 1, &res);
        copy_len = strnlen(buf, KPM_INTERNAL_LIST_LEN);
        if (res < 0) {
            /* Preserve KernelPatch's errno. */
        } else if (res >= KPM_INTERNAL_LIST_LEN ||
                   copy_len >= KPM_INTERNAL_LIST_LEN) {
            res = -EOVERFLOW;
        } else if (copy_len + 1 > len) {
            res = -ENOBUFS;
        } else if (copy_to_user((void __user *)arg1, buf, copy_len + 1)) {
            res = -EFAULT;
        } else {
            res = (int)copy_len;
        }
        kvfree(buf);
    } else if (control_code == SUKISU_KPM_CONTROL) {
        char kpm_name[KPM_NAME_LEN] = { 0 };
        char kpm_args[KPM_ARGS_LEN] = { 0 };
        long name_len;
        long arg_len = 0;

        if (!arg1 || !access_ok((void __user *)arg1, 1))
            goto invalid_arg;

        name_len = strncpy_from_user(kpm_name, (const char __user *)arg1,
                                     sizeof(kpm_name));
        if (name_len < 0)
            goto invalid_arg;
        if (!name_len || name_len >= sizeof(kpm_name)) {
            res = name_len ? -ENAMETOOLONG : -EINVAL;
            goto exit;
        }

        if (arg2) {
            if (!access_ok((void __user *)arg2, 1))
                goto invalid_arg;
            arg_len = strncpy_from_user(kpm_args, (const char __user *)arg2,
                                        sizeof(kpm_args));
            if (arg_len < 0)
                goto invalid_arg;
            if (arg_len >= sizeof(kpm_args)) {
                res = -E2BIG;
                goto exit;
            }
        }

        sukisu_kpm_control(kpm_name, kpm_args, arg_len, &res);
    } else if (control_code == SUKISU_KPM_VERSION) {
        char buffer[256] = { 0 };
        size_t outlen;
        size_t len;

        if (!arg1 || !arg2 || arg2 > KPM_LIST_LEN)
            goto invalid_arg;
        outlen = (size_t)arg2;
        if (!access_ok((void __user *)arg1, outlen))
            goto invalid_arg;

        sukisu_kpm_version(buffer, sizeof(buffer));
        len = strnlen(buffer, sizeof(buffer));
        if (len == sizeof(buffer))
            len = sizeof(buffer) - 1;
        if (len >= outlen)
            len = outlen - 1;
        buffer[len] = '\0';

        res = copy_to_user((void __user *)arg1, buffer, len + 1) ?
                  -EFAULT :
                  0;
    } else {
        res = -EINVAL;
    }

exit:
    if (!result_code ||
        copy_to_user((void __user *)result_code, &res, sizeof(res)))
        return -EFAULT;

    return 0;

invalid_arg:
    pr_err("kpm: invalid userspace argument: code=%lu arg1=%px arg2=%px\n",
           control_code, (void *)arg1, (void *)arg2);
    res = -EFAULT;
    goto exit;
}
EXPORT_SYMBOL(sukisu_handle_kpm);

int sukisu_is_kpm_control_code(unsigned long control_code)
{
    return (control_code >= CMD_KPM_CONTROL &&
            control_code <= CMD_KPM_CONTROL_MAX) ?
               1 :
               0;
}

int do_kpm(void __user *arg)
{
    struct ksu_kpm_cmd cmd;

    if (copy_from_user(&cmd, arg, sizeof(cmd))) {
        pr_err("kpm: copy_from_user failed\n");
        return -EFAULT;
    }

    if (!sukisu_is_kpm_control_code(cmd.control_code))
        return -EINVAL;

    if (!cmd.result_code ||
        !access_ok((void __user *)cmd.result_code, sizeof(int))) {
        pr_err("kpm: invalid result_code pointer %px\n",
               (void *)cmd.result_code);
        return -EFAULT;
    }

    return sukisu_handle_kpm(cmd.control_code, cmd.arg1, cmd.arg2,
                             cmd.result_code);
}
