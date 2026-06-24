/* susfs integration header for KinSU
 *
 * When CONFIG_KSU_SUSFS is defined, this includes the actual susfs headers.
 * Otherwise, it provides stub declarations.
 */

#ifndef _KSU_SUSFS_H
#define _KSU_SUSFS_H

#ifdef CONFIG_KSU_SUSFS
/* Actual susfs headers would be included here */
/* #include "susfs4ksu.h" */

int susfs_init(void);
void susfs_exit(void);

#else

static inline int susfs_init(void) { return 0; }
static inline void susfs_exit(void) {}

#endif /* CONFIG_KSU_SUSFS */

#endif /* _KSU_SUSFS_H */
