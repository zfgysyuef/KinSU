package com.mikokernel.ui.susfs.util

import android.content.Context
import android.content.SharedPreferences
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ShellUtils

object SuSFSManager {
    private const val PREFS_NAME = "susfs_prefs"
    private const val KEY_UNAME = "uname"
    private const val KEY_BUILD_TIME = "build_time"

    fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getUnameValue(context: Context) = getPrefs(context).getString(KEY_UNAME, "default") ?: "default"
    fun saveUnameValue(context: Context, v: String) = getPrefs(context).edit().putString(KEY_UNAME, v).apply()
    fun getBuildTimeValue(context: Context) = getPrefs(context).getString(KEY_BUILD_TIME, "default") ?: "default"
    fun saveBuildTimeValue(context: Context, v: String) = getPrefs(context).edit().putString(KEY_BUILD_TIME, v).apply()

    data class CommandResult(val isSuccess: Boolean, val output: String = "", val errorOutput: String = "")

    private fun runCmd(cmd: String): CommandResult {
        val result = Shell.getShell().newJob().add(cmd).exec()
        return CommandResult(result.isSuccess, result.out.joinToString("\n"), result.err.joinToString("\n"))
    }
}
