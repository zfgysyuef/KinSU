#include <linux/printk.h>
#include <linux/elf.h>
#include <uapi/linux/elf.h>
#include <asm/elf.h>
#include <linux/err.h>
#include <asm/insn.h>

#include "module.h"

#define le32_to_cpu(x) (x)
#define cpu_to_le32(x) (x)

enum aarch64_reloc_op {
    RELOC_OP_NONE,
    RELOC_OP_ABS,
    RELOC_OP_PREL,
    RELOC_OP_PAGE,
};

static u64 do_reloc(enum aarch64_reloc_op reloc_op, void *place, u64 val)
{
    switch (reloc_op) {
    case RELOC_OP_ABS:
        return val;
    case RELOC_OP_PREL:
        return val - (u64)place;
    case RELOC_OP_PAGE:
        return (val & ~0xfff) - ((u64)place & ~0xfff);
    case RELOC_OP_NONE:
        return 0;
    }
    pr_err("do_reloc: unknown relocation operation %d\n", reloc_op);
    return 0;
}

static int reloc_data(enum aarch64_reloc_op op, void *place, u64 val, int len)
{
    u64 imm_mask = (1ULL << len) - 1;
    s64 sval = do_reloc(op, place, val);

    switch (len) {
    case 16: *(s16 *)place = sval; break;
    case 32: *(s32 *)place = sval; break;
    case 64: *(s64 *)place = sval; break;
    default:
        pr_err("Invalid length (%d) for data relocation\n", len);
        return 0;
    }
    sval = (s64)(sval & ~(imm_mask >> 1)) >> (len - 1);
    if ((u64)(sval + 1) > 2) return -ERANGE;
    return 0;
}

static int reloc_insn_movw(enum aarch64_reloc_op op, void *place, u64 val,
                           int lsb, bool is_signed)
{
    u64 imm;
    s64 sval;
    u32 insn = le32_to_cpu(*(u32 *)place);

    sval = do_reloc(op, place, val);
    sval >>= lsb;
    imm = sval & 0xffff;

    if (is_signed) {
        insn &= ~(3 << 29);
        if ((s64)imm >= 0) {
            insn |= 2 << 29;
        } else {
            imm = ~imm;
        }
    }

    insn = aarch64_insn_encode_immediate(AARCH64_INSN_IMM_16, insn, imm);
    *(u32 *)place = cpu_to_le32(insn);

    sval >>= 16;
    if (is_signed) sval++;
    if ((u64)sval > (is_signed ? 1ULL : 0ULL)) return -ERANGE;
    return 0;
}

static int reloc_insn_imm(enum aarch64_reloc_op op, void *place, u64 val,
                          int lsb, int len, enum aarch64_insn_imm_type imm_type)
{
    u64 imm, imm_mask;
    s64 sval;
    u32 insn = le32_to_cpu(*(u32 *)place);

    sval = do_reloc(op, place, val);
    sval >>= lsb;
    imm_mask = (BIT(lsb + len) - 1) >> lsb;
    imm = sval & imm_mask;
    insn = aarch64_insn_encode_immediate(imm_type, insn, imm);
    *(u32 *)place = cpu_to_le32(insn);

    sval = (s64)(sval & ~(imm_mask >> 1)) >> (len - 1);
    if ((u64)(sval + 1) >= 2) return -ERANGE;
    return 0;
}

int kpm_apply_relocate(Elf64_Shdr *sechdrs, const char *strtab,
                       unsigned int symindex, unsigned int relsec,
                       struct kpm_module *me)
{
    return 0;
}

int kpm_apply_relocate_add(Elf64_Shdr *sechdrs, const char *strtab,
                           unsigned int symindex, unsigned int relsec,
                           struct kpm_module *me)
{
    unsigned int i;
    int ovf;
    bool overflow_check;
    Elf64_Sym *sym;
    void *loc;
    u64 val;
    Elf64_Rela *rel = (void *)sechdrs[relsec].sh_addr;

    for (i = 0; i < sechdrs[relsec].sh_size / sizeof(*rel); i++) {
        loc = (void *)sechdrs[sechdrs[relsec].sh_info].sh_addr + rel[i].r_offset;
        sym = (Elf64_Sym *)sechdrs[symindex].sh_addr + ELF64_R_SYM(rel[i].r_info);
        val = sym->st_value + rel[i].r_addend;

        overflow_check = true;

        switch (ELF64_R_TYPE(rel[i].r_info)) {
        case R_ARM_NONE:
        case R_AARCH64_NONE:
            ovf = 0;
            break;
        case R_AARCH64_ABS64:
            overflow_check = false;
            ovf = reloc_data(RELOC_OP_ABS, loc, val, 64);
            break;
        case R_AARCH64_ABS32:
            ovf = reloc_data(RELOC_OP_ABS, loc, val, 32);
            break;
        case R_AARCH64_ABS16:
            ovf = reloc_data(RELOC_OP_ABS, loc, val, 16);
            break;
        case R_AARCH64_PREL64:
            overflow_check = false;
            ovf = reloc_data(RELOC_OP_PREL, loc, val, 64);
            break;
        case R_AARCH64_PREL32:
            ovf = reloc_data(RELOC_OP_PREL, loc, val, 32);
            break;
        case R_AARCH64_PREL16:
            ovf = reloc_data(RELOC_OP_PREL, loc, val, 16);
            break;
        case R_AARCH64_MOVW_UABS_G0_NC:
            overflow_check = false;
        case R_AARCH64_MOVW_UABS_G0:
            ovf = reloc_insn_movw(RELOC_OP_ABS, loc, val, 0, false);
            break;
        case R_AARCH64_MOVW_UABS_G1_NC:
            overflow_check = false;
        case R_AARCH64_MOVW_UABS_G1:
            ovf = reloc_insn_movw(RELOC_OP_ABS, loc, val, 16, false);
            break;
        case R_AARCH64_MOVW_UABS_G2_NC:
            overflow_check = false;
        case R_AARCH64_MOVW_UABS_G2:
            ovf = reloc_insn_movw(RELOC_OP_ABS, loc, val, 32, false);
            break;
        case R_AARCH64_MOVW_UABS_G3:
            overflow_check = false;
            ovf = reloc_insn_movw(RELOC_OP_ABS, loc, val, 48, false);
            break;
        case R_AARCH64_MOVW_SABS_G0:
            ovf = reloc_insn_movw(RELOC_OP_ABS, loc, val, 0, true);
            break;
        case R_AARCH64_MOVW_SABS_G1:
            ovf = reloc_insn_movw(RELOC_OP_ABS, loc, val, 16, true);
            break;
        case R_AARCH64_MOVW_SABS_G2:
            ovf = reloc_insn_movw(RELOC_OP_ABS, loc, val, 32, true);
            break;
        case R_AARCH64_MOVW_PREL_G0_NC:
            overflow_check = false;
            ovf = reloc_insn_movw(RELOC_OP_PREL, loc, val, 0, false);
            break;
        case R_AARCH64_MOVW_PREL_G0:
            ovf = reloc_insn_movw(RELOC_OP_PREL, loc, val, 0, true);
            break;
        case R_AARCH64_MOVW_PREL_G1_NC:
            overflow_check = false;
            ovf = reloc_insn_movw(RELOC_OP_PREL, loc, val, 16, false);
            break;
        case R_AARCH64_MOVW_PREL_G1:
            ovf = reloc_insn_movw(RELOC_OP_PREL, loc, val, 16, true);
            break;
        case R_AARCH64_MOVW_PREL_G2_NC:
            overflow_check = false;
            ovf = reloc_insn_movw(RELOC_OP_PREL, loc, val, 32, false);
            break;
        case R_AARCH64_MOVW_PREL_G2:
            ovf = reloc_insn_movw(RELOC_OP_PREL, loc, val, 32, true);
            break;
        case R_AARCH64_MOVW_PREL_G3:
            overflow_check = false;
            ovf = reloc_insn_movw(RELOC_OP_PREL, loc, val, 48, true);
            break;
        case R_AARCH64_LD_PREL_LO19:
            ovf = reloc_insn_imm(RELOC_OP_PREL, loc, val, 2, 19, AARCH64_INSN_IMM_19);
            break;
        case R_AARCH64_ADR_PREL_LO21:
            ovf = reloc_insn_imm(RELOC_OP_PREL, loc, val, 0, 21, AARCH64_INSN_IMM_ADR);
            break;
        case R_AARCH64_ADR_PREL_PG_HI21_NC:
            overflow_check = false;
        case R_AARCH64_ADR_PREL_PG_HI21:
            ovf = reloc_insn_imm(RELOC_OP_PAGE, loc, val, 12, 21, AARCH64_INSN_IMM_ADR);
            break;
        case R_AARCH64_ADR_GOT_PAGE:
            overflow_check = false;
            ovf = reloc_insn_imm(RELOC_OP_PAGE, loc, val, 12, 21, AARCH64_INSN_IMM_ADR);
            break;
        case R_AARCH64_ADD_ABS_LO12_NC:
        case R_AARCH64_LDST8_ABS_LO12_NC:
            overflow_check = false;
            ovf = reloc_insn_imm(RELOC_OP_ABS, loc, val, 0, 12, AARCH64_INSN_IMM_12);
            break;
        case R_AARCH64_LDST16_ABS_LO12_NC:
            overflow_check = false;
            ovf = reloc_insn_imm(RELOC_OP_ABS, loc, val, 1, 11, AARCH64_INSN_IMM_12);
            break;
        case R_AARCH64_LDST32_ABS_LO12_NC:
            overflow_check = false;
            ovf = reloc_insn_imm(RELOC_OP_ABS, loc, val, 2, 10, AARCH64_INSN_IMM_12);
            break;
        case R_AARCH64_LDST64_ABS_LO12_NC:
            overflow_check = false;
            ovf = reloc_insn_imm(RELOC_OP_ABS, loc, val, 3, 9, AARCH64_INSN_IMM_12);
            break;
        case R_AARCH64_LDST128_ABS_LO12_NC:
            overflow_check = false;
            ovf = reloc_insn_imm(RELOC_OP_ABS, loc, val, 4, 8, AARCH64_INSN_IMM_12);
            break;
        case R_AARCH64_TSTBR14:
            ovf = reloc_insn_imm(RELOC_OP_PREL, loc, val, 2, 14, AARCH64_INSN_IMM_14);
            break;
        case R_AARCH64_CONDBR19:
            ovf = reloc_insn_imm(RELOC_OP_PREL, loc, val, 2, 19, AARCH64_INSN_IMM_19);
            break;
        case R_AARCH64_JUMP26:
        case R_AARCH64_CALL26:
            ovf = reloc_insn_imm(RELOC_OP_PREL, loc, val, 2, 26, AARCH64_INSN_IMM_26);
            break;
        default:
            pr_err("unsupported RELA relocation: %llu\n", ELF64_R_TYPE(rel[i].r_info));
            return -ENOEXEC;
        }

        if (overflow_check && ovf == -ERANGE)
            goto overflow;
    }
    return 0;

overflow:
    pr_err("overflow in relocation type %d val %llx\n",
           (int)ELF64_R_TYPE(rel[i].r_info), val);
    return -ENOEXEC;
}
