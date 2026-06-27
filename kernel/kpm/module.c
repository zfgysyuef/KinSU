#include <linux/err.h>
#include <linux/string.h>
#include <linux/kallsyms.h>
#include <linux/fs.h>
#include <linux/list.h>
#include <linux/kernel.h>
#include <linux/spinlock.h>
#include <linux/slab.h>
#include <linux/vmalloc.h>
#include <linux/rcupdate.h>
#include <linux/rculist.h>
#include <linux/set_memory.h>
#include <asm/cacheflush.h>

#include "module.h"
#include "relo.h"

#define SZ_128M 0x08000000

#define ALIGN_MASK(x, mask) (((x) + (mask)) & ~(mask))
#define ALIGN(x, a) ALIGN_MASK(x, (typeof(x))(a)-1)
#define align(x) ALIGN(x, PAGE_SIZE)

#define elf_check_arch(x) ((x)->e_machine == EM_AARCH64)

static inline bool strstarts(const char *str, const char *prefix)
{
    return strncmp(str, prefix, strlen(prefix)) == 0;
}

static char *next_string(char *string, unsigned long *secsize)
{
    while (string[0]) {
        string++;
        if ((*secsize)-- <= 1) return 0;
    }
    while (!string[0]) {
        string++;
        if ((*secsize)-- <= 1) return 0;
    }
    return string;
}

static long get_offset(struct kpm_module *mod, unsigned int *size,
                       Elf_Shdr *sechdr, unsigned int section)
{
    long ret = ALIGN(*size, sechdr->sh_addralign ?: 1);
    *size = ret + sechdr->sh_size;
    return ret;
}

static char *get_next_modinfo(const struct kpm_load_info *info,
                              const char *tag, char *prev)
{
    char *p;
    unsigned int taglen = strlen(tag);
    Elf_Shdr *infosec = &info->sechdrs[info->index.info];
    unsigned long size = infosec->sh_size;
    char *modinfo = (char *)info->hdr + infosec->sh_offset;
    if (prev) {
        size -= prev - modinfo;
        modinfo = next_string(prev, &size);
    }
    for (p = modinfo; p; p = next_string(p, &size)) {
        if (strncmp(p, tag, taglen) == 0 && p[taglen] == '=')
            return p + taglen + 1;
    }
    return 0;
}

static char *get_modinfo(const struct kpm_load_info *info, const char *tag)
{
    return get_next_modinfo(info, tag, 0);
}

static int find_sec(const struct kpm_load_info *info, const char *name)
{
    int i;
    for (i = 1; i < info->hdr->e_shnum; i++) {
        Elf_Shdr *shdr = &info->sechdrs[i];
        if ((shdr->sh_flags & SHF_ALLOC) &&
            strcmp(info->secstrings + shdr->sh_name, name) == 0)
            return i;
    }
    return 0;
}

static void *get_sh_base(struct kpm_load_info *info, const char *secname)
{
    int idx = find_sec(info, secname);
    if (!idx) return 0;
    Elf_Shdr *infosec = &info->sechdrs[idx];
    return (void *)info->hdr + infosec->sh_offset;
}

static unsigned long get_sh_size(struct kpm_load_info *info, const char *secname)
{
    int idx = find_sec(info, secname);
    if (!idx) return 0;
    Elf_Shdr *infosec = &info->sechdrs[idx];
    return infosec->sh_entsize;
}

static void layout_sections(struct kpm_module *mod, struct kpm_load_info *info)
{
    static unsigned long const masks[][2] = {
        { SHF_EXECINSTR | SHF_ALLOC, 0 },
        { SHF_ALLOC, SHF_WRITE },
        { SHF_WRITE | SHF_ALLOC, 0 },
        { 0, 0 }
    };
    int i, m;

    for (i = 0; i < info->hdr->e_shnum; i++)
        info->sechdrs[i].sh_entsize = ~0UL;

    for (m = 0; m < sizeof(masks) / sizeof(masks[0]); ++m) {
        for (i = 0; i < info->hdr->e_shnum; ++i) {
            Elf_Shdr *s = &info->sechdrs[i];
            if ((s->sh_flags & masks[m][0]) != masks[m][0] ||
                (s->sh_flags & masks[m][1]) ||
                s->sh_entsize != ~0UL)
                continue;
            s->sh_entsize = get_offset(mod, &mod->size, s, i);
        }
        switch (m) {
        case 0:
            mod->size = align(mod->size);
            mod->text_size = mod->size;
            break;
        case 1:
            mod->size = align(mod->size);
            mod->ro_size = mod->size;
            break;
        case 2:
            break;
        case 3:
            mod->size = align(mod->size);
            break;
        }
    }
}

static bool is_core_symbol(const Elf_Sym *src, const Elf_Shdr *sechdrs,
                           unsigned int shnum)
{
    const Elf_Shdr *sec;
    if (src->st_shndx == SHN_UNDEF || src->st_shndx >= shnum || !src->st_name)
        return false;
    sec = sechdrs + src->st_shndx;
    if (!(sec->sh_flags & SHF_ALLOC) || !(sec->sh_flags & SHF_EXECINSTR))
        return false;
    return true;
}

static int simplify_symbols(struct kpm_module *mod,
                            const struct kpm_load_info *info)
{
    Elf_Shdr *symsec = &info->sechdrs[info->index.sym];
    Elf_Sym *sym = (void *)symsec->sh_addr;
    unsigned long secbase;
    unsigned int i;
    int ret = 0;
    unsigned long addr = 0;

    for (i = 1; i < symsec->sh_size / sizeof(Elf_Sym); i++) {
        const char *name = info->strtab + sym[i].st_name;
        switch (sym[i].st_shndx) {
        case SHN_COMMON:
            if (!strncmp(name, "__gnu_lto", 9)) {
                pr_debug("kpm: Please compile with -fno-common\n");
                ret = -ENOEXEC;
            }
            break;
        case SHN_ABS:
            break;
        case SHN_UNDEF:
            addr = kallsyms_lookup_name(name);
            if (!addr) {
                pr_err("kpm: unknown symbol: %s\n", name);
                ret = -ENOENT;
                break;
            }
            sym[i].st_value = addr;
            break;
        default:
            secbase = info->sechdrs[sym[i].st_shndx].sh_addr;
            sym[i].st_value += secbase;
            break;
        }
    }
    return ret;
}

static int apply_relocations(struct kpm_module *mod,
                             const struct kpm_load_info *info)
{
    int rc = 0;
    unsigned int i;
    for (i = 1; i < info->hdr->e_shnum; i++) {
        unsigned int infosec = info->sechdrs[i].sh_info;
        if (infosec >= info->hdr->e_shnum) continue;
        if (!(info->sechdrs[infosec].sh_flags & SHF_ALLOC)) continue;
        if (info->sechdrs[i].sh_type == SHT_REL) {
            rc = kpm_apply_relocate(info->sechdrs, info->strtab,
                                    info->index.sym, i, mod);
        } else if (info->sechdrs[i].sh_type == SHT_RELA) {
            rc = kpm_apply_relocate_add(info->sechdrs, info->strtab,
                                        info->index.sym, i, mod);
        }
        if (rc < 0) break;
    }
    return rc;
}

static void layout_symtab(struct kpm_module *mod, struct kpm_load_info *info)
{
    Elf_Shdr *symsect = info->sechdrs + info->index.sym;
    Elf_Shdr *strsect = info->sechdrs + info->index.str;
    const Elf_Sym *src;
    unsigned int i, nsrc, ndst, strtab_size = 0;

    symsect->sh_flags |= SHF_ALLOC;
    symsect->sh_entsize = get_offset(mod, &mod->size, symsect,
                                     info->index.sym);

    src = (void *)info->hdr + symsect->sh_offset;
    nsrc = symsect->sh_size / sizeof(*src);

    strtab_size = 1;
    for (ndst = i = 0; i < nsrc; i++) {
        if (i == 0 ||
            is_core_symbol(src + i, info->sechdrs, info->hdr->e_shnum)) {
            strtab_size += strlen(&info->strtab[src[i].st_name]) + 1;
            ndst++;
        }
    }

    info->symoffs = ALIGN(mod->size, symsect->sh_addralign ?: 1);
    info->stroffs = mod->size = info->symoffs + ndst * sizeof(Elf_Sym);
    mod->size += strtab_size;

    strsect->sh_flags |= SHF_ALLOC;
    strsect->sh_entsize = get_offset(mod, &mod->size, strsect,
                                     info->index.str);
}

static int rewrite_section_headers(struct kpm_load_info *info)
{
    int i;
    info->sechdrs[0].sh_addr = 0;
    for (i = 1; i < info->hdr->e_shnum; i++) {
        Elf_Shdr *shdr = &info->sechdrs[i];
        if (shdr->sh_type != SHT_NOBITS &&
            info->len < shdr->sh_offset + shdr->sh_size) {
            return -ENOEXEC;
        }
        shdr->sh_addr = (size_t)info->hdr + shdr->sh_offset;
    }
    return 0;
}

static int move_module(struct kpm_module *mod, struct kpm_load_info *info)
{
    int i;
    pr_info("kpm: alloc module size: %x\n", mod->size);

    mod->start = vmalloc(mod->size);
    if (!mod->start) return -ENOMEM;
    memset(mod->start, 0, mod->size);

    /* Make all pages executable for KPM modules */
    set_memory_x((unsigned long)mod->start, mod->size >> PAGE_SHIFT);

    pr_debug("kpm: final section addresses:\n");
    for (i = 1; i < info->hdr->e_shnum; i++) {
        void *dest;
        Elf_Shdr *shdr = &info->sechdrs[i];
        if (!(shdr->sh_flags & SHF_ALLOC)) continue;

        dest = mod->start + shdr->sh_entsize;
        pr_debug("kpm:   %s %px %llx\n",
                 info->secstrings + shdr->sh_name, dest, shdr->sh_size);

        if (shdr->sh_type != SHT_NOBITS)
            memcpy(dest, (void *)shdr->sh_addr, shdr->sh_size);

        shdr->sh_addr = (unsigned long)dest;

        if (!mod->init && !strcmp(".kpm.init", info->secstrings + shdr->sh_name))
            mod->init = (kpm_initcall_t *)dest;
        if (!strcmp(".kpm.ctl0", info->secstrings + shdr->sh_name))
            mod->ctl0 = (kpm_ctl0call_t *)dest;
        if (!strcmp(".kpm.ctl1", info->secstrings + shdr->sh_name))
            mod->ctl1 = (kpm_ctl1call_t *)dest;
        if (!mod->exit && !strcmp(".kpm.exit", info->secstrings + shdr->sh_name))
            mod->exit = (kpm_exitcall_t *)dest;
        if (!mod->info.base && !strcmp(".kpm.info", info->secstrings + shdr->sh_name))
            mod->info.base = (const char *)dest;
    }
    mod->info.name = info->info.name - info->info.base + mod->info.base;
    mod->info.version = info->info.version - info->info.base + mod->info.base;

    if (info->info.license)
        mod->info.license = info->info.license - info->info.base + mod->info.base;
    if (info->info.author)
        mod->info.author = info->info.author - info->info.base + mod->info.base;
    if (info->info.description)
        mod->info.description = info->info.description - info->info.base + mod->info.base;

    return 0;
}

static int setup_load_info(struct kpm_load_info *info)
{
    int rc = 0;
    int i;
    info->sechdrs = (void *)info->hdr + info->hdr->e_shoff;
    info->secstrings = (void *)info->hdr +
        info->sechdrs[info->hdr->e_shstrndx].sh_offset;

    rc = rewrite_section_headers(info);
    if (rc) {
        pr_err("kpm: rewrite section error\n");
        return rc;
    }

    if (!find_sec(info, ".kpm.init") || !find_sec(info, ".kpm.exit")) {
        pr_err("kpm: no .kpm.init or .kpm.exit section\n");
        return -ENOEXEC;
    }

    info->index.info = find_sec(info, ".kpm.info");
    if (!info->index.info) {
        pr_err("kpm: no .kpm.info section\n");
        return -ENOEXEC;
    }
    info->info.base = get_sh_base(info, ".kpm.info");
    info->info.size = get_sh_size(info, ".kpm.info");

    {
        const char *name = get_modinfo(info, "name");
        if (!name) {
            pr_err("kpm: module name not found\n");
            return -ENOEXEC;
        }
        info->info.name = name;
        pr_debug("kpm: loading module:\n  name: %s\n", name);

        {
            const char *version = get_modinfo(info, "version");
            if (!version) {
                pr_debug("kpm: module version not found\n");
                return -ENOEXEC;
            }
            info->info.version = version;
            pr_debug("  version: %s\n", version);
        }
    }

    info->info.license = get_modinfo(info, "license");
    info->info.author = get_modinfo(info, "author");
    info->info.description = get_modinfo(info, "description");

    for (i = 1; i < info->hdr->e_shnum; i++) {
        if (info->sechdrs[i].sh_type == SHT_SYMTAB) {
            info->index.sym = i;
            info->index.str = info->sechdrs[i].sh_link;
            info->strtab = (char *)info->hdr +
                info->sechdrs[info->index.str].sh_offset;
            break;
        }
    }

    if (info->index.sym == 0) {
        pr_debug("kpm: module has no symbols (stripped?)\n");
        return -ENOEXEC;
    }
    return 0;
}

static int elf_header_check(struct kpm_load_info *info)
{
    if (info->len <= sizeof(*(info->hdr))) return -ENOEXEC;
    if (memcmp(info->hdr->e_ident, ELFMAG, SELFMAG) ||
        info->hdr->e_type != ET_REL ||
        !elf_check_arch(info->hdr) ||
        info->hdr->e_shentsize != sizeof(Elf_Shdr))
        return -ENOEXEC;
    if (info->hdr->e_shoff >= info->len ||
        (info->hdr->e_shnum * sizeof(Elf_Shdr) > info->len - info->hdr->e_shoff))
        return -ENOEXEC;
    return 0;
}

static struct kpm_module modules = { 0 };
static DEFINE_SPINLOCK(module_lock);

long kpm_load_module(const void *data, int len, const char *args,
                     const char *event, void *__user reserved)
{
    struct kpm_load_info load_info = { .len = len, .hdr = data };
    struct kpm_load_info *info = &load_info;
    struct kpm_module *mod;
    long rc = 0;

    rc = elf_header_check(info);
    if (rc) goto out;
    rc = setup_load_info(info);
    if (rc) goto out;

    if (kpm_find_module(info->info.name)) {
        pr_err("kpm: %s already loaded\n", info->info.name);
        rc = -EEXIST;
        goto out;
    }

    mod = kvmalloc(sizeof(struct kpm_module), GFP_KERNEL);
    if (!mod) return -ENOMEM;
    memset(mod, 0, sizeof(struct kpm_module));

    if (args) {
        mod->args = kvmalloc(strlen(args) + 1, GFP_KERNEL);
        if (!mod->args) {
            rc = -ENOMEM;
            goto free1;
        }
        strcpy(mod->args, args);
    }

    layout_sections(mod, info);
    layout_symtab(mod, info);

    rc = move_module(mod, info);
    if (rc) goto free;
    rc = simplify_symbols(mod, info);
    if (rc) goto free;
    rc = apply_relocations(mod, info);
    if (rc) goto free;

    flush_icache_range((unsigned long)mod->start,
                       (unsigned long)mod->start + mod->size);

    rc = (*mod->init)(mod->args, event, reserved);

    if (!rc) {
        pr_info("kpm: [%s] loaded with args [%s]\n", mod->info.name, args);
        list_add_tail(&mod->list, &modules.list);
        goto out;
    } else {
        pr_info("kpm: [%s] init failed with %ld, trying exit...\n",
                mod->info.name, rc);
        (*mod->exit)(reserved);
    }

free:
    if (mod->args) kvfree(mod->args);
    if (mod->start) {
        set_memory_nx((unsigned long)mod->start, mod->size >> PAGE_SHIFT);
        vfree(mod->start);
    }
free1:
    kvfree(mod);
out:
    return rc;
}

long kpm_unload_module(const char *name, void *__user reserved)
{
    struct kpm_module *mod;
    long rc = 0;

    if (!name) return -EINVAL;
    pr_info("kpm: unloading: %s\n", name);

    rcu_read_lock();
    mod = kpm_find_module(name);
    if (!mod) {
        rc = -ENOENT;
        goto out;
    }
    list_del(&mod->list);
    rc = (*mod->exit)(reserved);

    if (mod->args) kvfree(mod->args);
    if (mod->ctl_args) kvfree(mod->ctl_args);

    if (mod->start) {
        set_memory_nx((unsigned long)mod->start, mod->size >> PAGE_SHIFT);
        vfree(mod->start);
    }
    kvfree(mod);

    pr_info("kpm: unloaded %s, rc=%ld\n", name, rc);

out:
    rcu_read_unlock();
    return rc;
}

long kpm_load_module_path(const char *path, const char *args,
                          void *__user reserved)
{
    long rc = 0;
    struct file *filp;
    loff_t len, pos;
    void *data;

    pr_info("kpm: loading %s\n", path);
    if (!path) return -EINVAL;

    filp = filp_open(path, O_RDONLY, 0);
    if (unlikely(!filp || IS_ERR(filp))) {
        pr_err("kpm: open module %s error\n", path);
        rc = PTR_ERR(filp);
        goto out;
    }
    len = vfs_llseek(filp, 0, SEEK_END);
    pr_debug("kpm: module size: %llx\n", len);
    vfs_llseek(filp, 0, SEEK_SET);

    data = vmalloc(len);
    if (!data) {
        rc = -ENOMEM;
        goto close;
    }
    memset(data, 0, len);

    pos = 0;
    rc = kernel_read(filp, data, len, &pos);
    if (rc < 0 || pos != len) {
        pr_err("kpm: read module %s error\n", path);
        rc = rc < 0 ? rc : -EIO;
        vfree(data);
        goto close;
    }

    rc = kpm_load_module(data, len, args, "load-file", reserved);
    vfree(data);
close:
    filp_close(filp, 0);
out:
    return rc;
}

long kpm_module_control0(const char *name, const char *ctl_args,
                         char *__user out_msg, int outlen)
{
    struct kpm_module *mod;
    int args_len;
    long rc = 0;

    if (!name || !ctl_args) return -EINVAL;
    args_len = strlen(ctl_args);
    if (args_len <= 0) return -EINVAL;

    pr_info("kpm: control %s args=%s\n", name, ctl_args);

    rcu_read_lock();
    mod = kpm_find_module(name);
    if (!mod) {
        rc = -ENOENT;
        goto out;
    }

    if (!mod->ctl0 || !*mod->ctl0) {
        pr_err("kpm: %s has no ctl0\n", name);
        rc = -ENOSYS;
        goto out;
    }

    if (mod->ctl_args) kvfree(mod->ctl_args);
    mod->ctl_args = kvmalloc(args_len + 1, GFP_KERNEL);
    if (!mod->ctl_args) {
        rc = -ENOMEM;
        goto out;
    }
    strcpy(mod->ctl_args, ctl_args);

    rc = (*mod->ctl0)(mod->ctl_args, out_msg, outlen);
    pr_info("kpm: control %s rc=%ld\n", name, rc);

out:
    rcu_read_unlock();
    return rc;
}

long kpm_module_control1(const char *name, void *a1, void *a2, void *a3)
{
    struct kpm_module *mod;
    long rc = 0;

    pr_info("kpm: control1 %s a1=%px a2=%px a3=%px\n", name, a1, a2, a3);

    rcu_read_lock();
    mod = kpm_find_module(name);
    if (!mod) {
        rc = -ENOENT;
        goto out;
    }

    if (!mod->ctl1 || !*mod->ctl1) {
        pr_err("kpm: %s has no ctl1\n", name);
        rc = -ENOSYS;
        goto out;
    }

    rc = (*mod->ctl1)(a1, a2, a3);
    pr_info("kpm: control1 %s rc=%ld\n", name, rc);

out:
    rcu_read_unlock();
    return rc;
}

struct kpm_module *kpm_find_module(const char *name)
{
    struct kpm_module *pos;
    list_for_each_entry(pos, &modules.list, list) {
        if (!strcmp(name, pos->info.name))
            return pos;
    }
    return 0;
}

int kpm_get_module_nums(void)
{
    struct kpm_module *pos;
    int n = 0;
    rcu_read_lock();
    list_for_each_entry(pos, &modules.list, list) { n++; }
    rcu_read_unlock();
    return n;
}

int kpm_list_modules(char *out_names, int size)
{
    struct kpm_module *pos;
    int off = 0;
    rcu_read_lock();
    list_for_each_entry(pos, &modules.list, list) {
        off += snprintf(out_names + off, size - 1 - off, "%s\n",
                        pos->info.name);
    }
    if (off > 0) out_names[off - 1] = '\0';
    rcu_read_unlock();
    return off;
}

int kpm_get_module_info(const char *name, char *out_info, int size)
{
    struct kpm_module *mod;
    int sz;

    if (size <= 0) return 0;

    rcu_read_lock();
    mod = kpm_find_module(name);
    if (!mod) {
        rcu_read_unlock();
        return -ENOENT;
    }

    sz = snprintf(out_info, size - 1,
                  "name=%s\nversion=%s\nlicense=%s\nauthor=%s\n"
                  "description=%s\nargs=%s\n",
                  mod->info.name, mod->info.version, mod->info.license,
                  mod->info.author, mod->info.description, mod->args);

    if (sz > 0) out_info[sz - 1] = '\0';
    rcu_read_unlock();
    return sz;
}

void kpm_module_init(void)
{
    INIT_LIST_HEAD(&modules.list);
}

void kpm_module_exit(void)
{
    struct kpm_module *mod, *tmp;
    list_for_each_entry_safe(mod, tmp, &modules.list, list) {
        pr_info("kpm: force unloading %s\n", mod->info.name);
        list_del(&mod->list);
        (*mod->exit)(NULL);
        if (mod->args) kvfree(mod->args);
        if (mod->ctl_args) kvfree(mod->ctl_args);
        if (mod->start) {
            set_memory_nx((unsigned long)mod->start,
                          mod->size >> PAGE_SHIFT);
            vfree(mod->start);
        }
        kvfree(mod);
    }
}
