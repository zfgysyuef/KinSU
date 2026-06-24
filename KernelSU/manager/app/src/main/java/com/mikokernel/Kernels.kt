package com.mikokernel

import android.system.Os

/**
 * @author weishu
 * @date 2022/12/10.
 */

data class KernelVersion(val major: Int, val patchLevel: Int, val subLevel: Int) {
    override fun toString(): String {
        return "$major.$patchLevel.$subLevel"
    }

    fun isGKI(): Boolean {
        // kernel 6.x
        if (major > 5) {
            return true
        }

        // kernel 5.10.x
        if (major == 5) {
            return patchLevel >= 10
        }

        return false
    }
}

/**
 * Check if the device is a GKI device.
 * This is more reliable than just checking kernel version,
 * because third-party kernels may have non-GKI version strings
 * on GKI devices.
 */
fun isGkiDevice(): Boolean {
    // First check kernel version (fast, no root needed)
    val kernelGki = getKernelVersion().isGKI()
    if (kernelGki) return true

    // If kernel version says non-GKI, check for init_boot partition
    // (GKI devices have init_boot, non-GKI devices don't)
    return try {
        java.io.File("/dev/block/by-name/init_boot").exists()
    } catch (_: Exception) {
        false
    }
}

fun parseKernelVersion(version: String): KernelVersion {
    val find = "(\\d+)\\.(\\d+)\\.(\\d+)".toRegex().find(version)
    return if (find != null) {
        KernelVersion(find.groupValues[1].toInt(), find.groupValues[2].toInt(), find.groupValues[3].toInt())
    } else {
        KernelVersion(-1, -1, -1)
    }
}

fun getKernelVersion(): KernelVersion {
    Os.uname().release.let {
        return parseKernelVersion(it)
    }
}