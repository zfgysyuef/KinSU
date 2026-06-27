#ifndef _KSU_KPM_RELO_H_
#define _KSU_KPM_RELO_H_

#include <linux/elf.h>

struct kpm_module;

int kpm_apply_relocate_add(Elf64_Shdr *sechdrs, const char *strtab,
                           unsigned int symindex, unsigned int relsec,
                           struct kpm_module *me);
int kpm_apply_relocate(Elf64_Shdr *sechdrs, const char *strtab,
                       unsigned int symindex, unsigned int relsec,
                       struct kpm_module *me);

#endif
