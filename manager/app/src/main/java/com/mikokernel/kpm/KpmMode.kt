package com.mikokernel.kpm

import android.content.Context

/**
 * KPM / GKI 模式标记
 *
 * 当用户通过 PatchKernel + 开启 KPM 安装后，设备进入 GKI 模式，
 * KernelPatch supercall 接口可用。此时：
 * - Root 授权和常规模块仍由 KinSU (libKinSUd.so) 处理
 * - KPM 内核补丁模块 (.kpm) 由 APatch daemon (libapd.so) 处理
 *
 * apd 只负责 KPM 模块的加载/卸载/管理，不接管其他任何功能。
 */
object KpmMode {
    private var active: Boolean = false

    /** 激活 GKI/KPM 模式（安装成功时调用） */
    fun activate() {
        active = true
    }

    /** 关闭 GKI/KPM 模式 */
    fun deactivate() {
        active = false
    }

    /** 当前是否处于 GKI/KPM 模式（KernelPatch supercall 可用） */
    fun isActive(): Boolean = active

    /** 持久化到 SharedPreferences */
    fun persist(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_KPM_ACTIVE, active)
            .apply()
    }

    /**
     * 从 SharedPreferences 恢复模式状态
     * @return 恢复后的 active 状态
     */
    fun restore(context: Context): Boolean {
        active = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_KPM_ACTIVE, false)
        return active
    }

    private const val PREFS_NAME = "kpm_mode"
    private const val KEY_KPM_ACTIVE = "kpm_active"
}
