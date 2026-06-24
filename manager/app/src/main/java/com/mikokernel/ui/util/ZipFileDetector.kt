package com.mikokernel.ui.util

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.zip.ZipInputStream

enum class ZipType { MODULE, KERNEL, UNKNOWN }

data class ZipFileInfo(
    val uri: String,
    val type: ZipType,
    val name: String = "",
    val version: String = "",
    val author: String = "",
    val description: String = "",
    val supported: String = ""
)

object ZipFileDetector {
    fun detectZipType(context: Context, uri: Uri): ZipType {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipStream ->
                    var hasModuleProp = false
                    var hasToolsFolder = false
                    var hasAnykernelSh = false
                    var entry = zipStream.nextEntry
                    while (entry != null) {
                        val name = entry.name.lowercase()
                        when {
                            name == "module.prop" || name.endsWith("/module.prop") -> hasModuleProp = true
                            name.startsWith("tools/") -> hasToolsFolder = true
                            name == "anykernel.sh" || name.endsWith("/anykernel.sh") -> hasAnykernelSh = true
                        }
                        zipStream.closeEntry()
                        entry = zipStream.nextEntry
                    }
                    when {
                        hasModuleProp -> ZipType.MODULE
                        hasToolsFolder && hasAnykernelSh -> ZipType.KERNEL
                        else -> ZipType.UNKNOWN
                    }
                }
            } ?: ZipType.UNKNOWN
        } catch (e: IOException) { ZipType.UNKNOWN }
    }

    fun parseKernelInfo(context: Context, uri: Uri): ZipFileInfo {
        var zipInfo = ZipFileInfo(uri = uri.toString(), type = ZipType.KERNEL)
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipStream ->
                    var entry = zipStream.nextEntry
                    while (entry != null) {
                        if (entry.name.lowercase() == "anykernel.sh" || entry.name.endsWith("/anykernel.sh")) {
                            val reader = BufferedReader(InputStreamReader(zipStream))
                            mapOf<String, String>()
                            var line = reader.readLine()
                            while (line != null) {
                                if (line.contains("kernel.string=")) {
                                    val v = line.substringAfter("kernel.string=").trim().removeSurrounding("\"").removeSurrounding("'")
                                    val sc = v.indexOf(';'); if (sc > 0) zipInfo = zipInfo.copy(name = v.substring(0, sc).trim()) else zipInfo = zipInfo.copy(name = v)
                                }
                                if (line.contains("supported.versions=")) {
                                    val v = line.substringAfter("supported.versions=").trim().removeSurrounding("\"").removeSurrounding("'")
                                    val sc = v.indexOf(';'); if (sc > 0) zipInfo = zipInfo.copy(supported = v.substring(0, sc).trim()) else zipInfo = zipInfo.copy(supported = v)
                                }
                                if (line.contains("kernel.version=")) {
                                    val v = line.substringAfter("kernel.version=").trim().removeSurrounding("\"").removeSurrounding("'")
                                    val sc = v.indexOf(';'); if (sc > 0) zipInfo = zipInfo.copy(version = v.substring(0, sc).trim()) else zipInfo = zipInfo.copy(version = v)
                                }
                                if (line.contains("kernel.author=")) {
                                    val v = line.substringAfter("kernel.author=").trim().removeSurrounding("\"").removeSurrounding("'")
                                    val sc = v.indexOf(';'); if (sc > 0) zipInfo = zipInfo.copy(author = v.substring(0, sc).trim()) else zipInfo = zipInfo.copy(author = v)
                                }
                                line = reader.readLine()
                            }
                            break
                        }
                        zipStream.closeEntry()
                        entry = zipStream.nextEntry
                    }
                }
            }
        } catch (_: Exception) {}
        return zipInfo
    }
}
