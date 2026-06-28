package com.mikokernel.ui.util

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import com.mikokernel.ksuApp
import com.mikokernel.ui.util.module.LatestVersionInfo
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * @author weishu
 * @date 2023/6/22.
 */
suspend fun download(
    url: String,
    fileName: String,
    onDownloaded: (Uri) -> Unit = {},
    onDownloading: () -> Unit = {},
    onProgress: (Int) -> Unit = {}
) {
    onDownloading()

    val downloadId = DownloadManager.enqueue(
        context = ksuApp,
        url = url,
        fileName = fileName,
        onCompleted = onDownloaded,
    )

    DownloadManager.downloads
        .onEach { map -> map[downloadId]?.let { onProgress(it.progress) } }
        .first { map ->
            val status = map[downloadId]?.status
            status == DownloadManager.Status.COMPLETED ||
                status == DownloadManager.Status.FAILED
        }
}

suspend fun checkNewVersion(): LatestVersionInfo {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/Spring-bulid/KinSU/releases/latest")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github+json")
                connectTimeout = 10000
                readTimeout = 10000
            }
            conn.inputStream.bufferedReader().use { reader ->
                val json = JSONObject(reader.readText())
                val tagName = json.optString("tag_name", "")
                val body = json.optString("body", "")

                // Find first .apk asset: extract download URL and parse versionCode from filename
                // APK filename format: KinSU_<versionName>_<versionCode>-release.apk
                // e.g. KinSU_3.0.2_30020-release.apk -> versionCode = 30020
                var downloadUrl = ""
                var apkAssetName = ""
                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            downloadUrl = asset.optString("browser_download_url", "")
                            apkAssetName = name
                            break
                        }
                    }
                }

                // Parse versionCode: prefer APK filename, fallback to tag_name (legacy v30021 format)
                val versionCodeFromApk = Regex("_(\\d+)-release\\.apk$").find(apkAssetName)?.let {
                    it.groupValues[1].toIntOrNull()
                }
                val versionCodeFromTag = tagName.removePrefix("v").toIntOrNull()
                val versionCode = versionCodeFromApk ?: versionCodeFromTag ?: 0

                // versionName: prefer tag (without v prefix), fallback to APK filename segment
                val versionNameFromTag = tagName.removePrefix("v")
                val versionNameFromApk = Regex("^([\\d.]+)_").find(apkAssetName)?.let {
                    it.groupValues[1]
                }
                val versionName = when {
                    versionNameFromTag.isNotEmpty() -> versionNameFromTag
                    !versionNameFromApk.isNullOrEmpty() -> versionNameFromApk
                    else -> "v$versionCode"
                }

                LatestVersionInfo(
                    versionCode = versionCode,
                    versionName = versionName,
                    downloadUrl = downloadUrl,
                    changelog = body,
                )
            }
        } catch (_: Throwable) {
            LatestVersionInfo()
        }
    }
}
