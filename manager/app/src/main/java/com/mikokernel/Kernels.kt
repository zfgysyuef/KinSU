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
 * on GKI devices, and SuSFS uname spoofing can also fake the
 * kernel version string.
 */
fun isGkiDevice(): Boolean {
    // 优先检测 init_boot 分区（硬件事实，不受 SuSFS uname 伪装影响）
    // GKI 设备必有 init_boot 分区，非 GKI 设备没有
    if (hasInitBootPartition()) return true

    // 再用内核版本作为辅助判断
    // 注意：如果用户通过 SuSFS 伪装了 uname，这里可能返回 false
    // 但 init_boot 检测已经能覆盖这种情况
    return getKernelVersion().isGKI()
}

/**
 * 检测设备是否存在 init_boot 分区。
 * 尝试多个可能的 by-name 路径以提高兼容性。
 */
private fun hasInitBootPartition(): Boolean {
    val paths = listOf(
        "/dev/block/by-name/init_boot",
        "/dev/block/bootdevice/by-name/init_boot",
    )
    return try {
        paths.any { java.io.File(it).exists() }
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