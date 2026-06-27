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
                // Parse version code from tag like "v30021" -> 30021
                val versionCode = tagName.removePrefix("v").toIntOrNull() ?: 0
                val versionName = tagName.ifEmpty { "v$versionCode" }
                val body = json.optString("body", "")
                // Find first .apk asset download URL
                var downloadUrl = ""
                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            downloadUrl = asset.optString("browser_download_url", "")
                            break
                        }
                    }
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
