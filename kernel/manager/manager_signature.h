/* Auto-generated manager signature for KinSU
 *
 * These values identify the KinSU manager APK.
 * The kernel module uses these to verify the manager app
 * when searching /data/app for the correct APK.
 *
 * Regenerate with: python scripts/extract_sig.py
 */

#ifndef __KSU_MANAGER_SIGNATURE_H
#define __KSU_MANAGER_SIGNATURE_H

/* Manager package name */
#ifndef KSU_MANAGER_PACKAGE
#define KSU_MANAGER_PACKAGE "com.mikokernel"
#endif

/* APK v2 signing certificate size in bytes (724 = 0x2d4) */
#ifndef EXPECTED_SIZE
#define EXPECTED_SIZE 0x2d4
#endif

/* SHA256 hash of the APK v2 signing certificate */
#ifndef EXPECTED_HASH
#define EXPECTED_HASH "eb4ae49882e0ea80c13989ea2141368b1f819e2bfe10801b3c15b743bd647dab"
#endif

/*
 * To add a fallback signature (e.g., for debug builds or re-signed APKs),
 * define EXPECTED_SIZE2 and EXPECTED_HASH2 before including this file,
 * or pass them as compiler flags:
 *   KSU_EXPECTED_SIZE2=... KSU_EXPECTED_HASH2=...
 */

#endif /* __KSU_MANAGER_SIGNATURE_H */
