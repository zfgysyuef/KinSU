package com.mikokernel.ui.util.module

import com.mikokernel.ksuApp
import com.mikokernel.ui.util.isNetworkAvailable
import okhttp3.Request
import org.json.JSONObject

data class ModuleDetail(
    val readme: String,
    val readmeHtml: String,
    val latestTag: String,
    val latestTime: String,
    val latestAssetName: String?,
    val latestAssetUrl: String?,
    val releases: List<ReleaseInfo>,
    val homepageUrl: String,
    val sourceUrl: String,
    val url: String
)

data class ReleaseInfo(
    val name: String,
    val tagName: String,
    val publishedAt: String,
    val descriptionHTML: String,
    val assets: List<ReleaseAssetInfo>
)

data class ReleaseAssetInfo(
    val name: String,
    val downloadUrl: String,
    val size: Long,
    val downloadCount: Int
)

fun sanitizeVersionString(version: String): String {
    return version.replace(Regex("[^a-zA-Z0-9.\\-_]"), "_")
}

fun stripTicks(s: String): String {
    val t = s.trim()
    return if (t.startsWith("`") && t.endsWith("`") && t.length >= 2) t.substring(1, t.length - 1) else t
}

private const val MODULES_BASE_URL = "https://raw.githubusercontent.com/Spring-bulid/KinSU-Modules/main"

fun fetchReleaseDescriptionHtml(moduleId: String, latestTag: String): String? {
    return try {
        if (!isNetworkAvailable(ksuApp)) return null
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        val url = "$MODULES_BASE_URL/modules/$moduleId/releases/$latestTag.md"
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        if (response.isSuccessful) response.body?.string() else null
    } catch (_: Throwable) {
        null
    }
}

fun fetchModuleDetail(moduleId: String): ModuleDetail? {
    return try {
        if (!isNetworkAvailable(ksuApp)) return null
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        val url = "$MODULES_BASE_URL/modules/$moduleId/detail.json"
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return null
        val body = response.body?.string() ?: return null
        val json = JSONObject(body)
        ModuleDetail(
            readme = json.optString("readme", ""),
            readmeHtml = json.optString("readmeHtml", ""),
            latestTag = json.optString("latestTag", ""),
            latestTime = json.optString("latestTime", ""),
            latestAssetName = json.optString("latestAssetName", null),
            latestAssetUrl = json.optString("latestAssetUrl", null),
            releases = emptyList(),
            homepageUrl = json.optString("homepageUrl", ""),
            sourceUrl = json.optString("sourceUrl", ""),
            url = json.optString("url", "")
        )
    } catch (_: Throwable) {
        null
    }
}
