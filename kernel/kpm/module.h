#ifndef _KSU_KPM_MODULE_H_
#define _KSU_KPM_MODULE_H_

#include <linux/elf.h>
#include <linux/list.h>
#include <linux/types.h>

/* Callback signatures matching KernelPatch KPM ABI */
typedef long (*kpm_initcall_t)(const char *args, const char *event, void *__user reserved);
typedef long (*kpm_exitcall_t)(void *__user reserved);
typedef long (*kpm_ctl0call_t)(const char *ctl_args, char *__user out_msg, int outlen);
typedef long (*kpm_ctl1call_t)(void *a1, void *a2, void *a3);

struct kpm_info {
    const char *base, *name, *version, *license, *author, *description;
    unsigned long size;
};

struct kpm_load_info {
    struct kpm_info info;
    const Elf_Ehdr *hdr;
    unsigned long len;
    Elf_Shdr *sechdrs;
    char *secstrings, *strtab;
    unsigned long symoffs, stroffs;
    struct {
        unsigned int sym, str, mod, info;
    } index;
};

struct kpm_module {
    struct kpm_info info;
    char *args, *ctl_args;
    kpm_initcall_t *init;
    kpm_ctl0call_t *ctl0;
    kpm_ctl1call_t *ctl1;
    kpm_exitcall_t *exit;
    unsigned int size, text_size, ro_size;
    void *start;
    struct list_head list;
};

long kpm_load_module(const void *data, int len, const char *args, const char *event, void *__user reserved);
long kpm_load_module_path(const char *path, const char *args, void *__user reserved);
long kpm_module_control0(const char *name, const char *ctl_args, char *__user out_msg, int outlen);
long kpm_module_control1(const char *name, void *a1, void *a2, void *a3);
long kpm_unload_module(const char *name, void *__user reserved);
struct kpm_module *kpm_find_module(const char *name);

int kpm_get_module_nums(void);
int kpm_list_modules(char *out_names, int size);
int kpm_get_module_info(const char *name, char *out_info, int size);

void kpm_module_init(void);
void kpm_module_exit(void);

#endif
