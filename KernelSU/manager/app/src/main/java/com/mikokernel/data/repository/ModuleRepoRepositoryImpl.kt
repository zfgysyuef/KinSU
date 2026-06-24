package com.mikokernel.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.mikokernel.data.model.Author
import com.mikokernel.data.model.ReleaseAsset
import com.mikokernel.data.model.RepoModule
import com.mikokernel.ksuApp
import com.mikokernel.ui.util.isNetworkAvailable
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class ModuleRepoRepositoryImpl : ModuleRepoRepository {

    companion object {
        private const val MODULES_URL = "https://raw.githubusercontent.com/Spring-bulid/KinSU-Modules/main/modules.json"
    }

    override suspend fun fetchModules(): Result<List<RepoModule>> = withContext(Dispatchers.IO) {
        try {
            if (!isNetworkAvailable(ksuApp)) return@withContext Result.success(emptyList())
            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            val request = Request.Builder().url(MODULES_URL).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext Result.success(emptyList())
            val body = response.body?.string() ?: return@withContext Result.success(emptyList())
            val array = JSONArray(body)
            val modules = (0 until array.length()).mapNotNull { idx ->
                parseRepoModule(array.optJSONObject(idx))
            }
            Result.success(modules)
        } catch (_: Throwable) {
            Result.success(emptyList())
        }
    }

    private fun parseRepoModule(item: JSONObject): RepoModule? {
        val moduleId = item.optString("moduleId", "")
        if (moduleId.isEmpty()) return null
        val moduleName = item.optString("moduleName", "")
        val authorsArray = item.optJSONArray("authors")
        val authorList = if (authorsArray != null) {
            (0 until authorsArray.length())
                .mapNotNull { idx ->
                    val authorObj = authorsArray.optJSONObject(idx) ?: return@mapNotNull null
                    val name = authorObj.optString("name", "").trim()
                    var link = authorObj.optString("link", "").trim()
                    if (link.startsWith("`") && link.endsWith("`") && link.length >= 2) {
                        link = link.substring(1, link.length - 1)
                    }
                    if (name.isEmpty()) null else Author(name = name, link = link)
                }
        } else {
            emptyList()
        }
        val authors = if (authorList.isNotEmpty()) authorList.joinToString(", ") { it.name } else item.optString("authors", "")
        val summary = item.optString("summary", "")
        val metamodule = item.optBoolean("metamodule", false)
        val stargazerCount = item.optInt("stargazerCount", 0)
        val updatedAt = item.optString("updatedAt", "")
        val createdAt = item.optString("createdAt", "")

        var latestRelease = ""
        var latestReleaseTime = ""
        var latestVersionCode = 0
        var latestAsset: ReleaseAsset? = null
        val lr = item.optJSONObject("latestRelease")
        if (lr != null) {
            val lrName = lr.optString("name", lr.optString("version", ""))
            val lrTime = lr.optString("time", "")
            var lrUrl = lr.optString("downloadUrl", "")
            lrUrl = lrUrl.trim().let {
                var s = it
                if (s.startsWith("`") && s.endsWith("`") && s.length >= 2) {
                    s = s.substring(1, s.length - 1)
                }
                s
            }
            val vcAny = lr.opt("versionCode")
            latestVersionCode = when (vcAny) {
                is Number -> vcAny.toInt()
                is String -> vcAny.toIntOrNull() ?: 0
                else -> 0
            }
            latestRelease = lrName
            latestReleaseTime = lrTime
            if (lrUrl.isNotEmpty()) {
                val fileName = lrUrl.substringAfterLast('/')
                latestAsset = ReleaseAsset(name = fileName, downloadUrl = lrUrl, size = 0L)
            }
        }

        return RepoModule(
            moduleId = moduleId,
            moduleName = moduleName,
            authors = authors,
            authorList = authorList,
            summary = summary,
            metamodule = metamodule,
            stargazerCount = stargazerCount,
            updatedAt = updatedAt,
            createdAt = createdAt,
            latestRelease = latestRelease,
            latestReleaseTime = latestReleaseTime,
            latestVersionCode = latestVersionCode,
            latestAsset = latestAsset,
        )
    }
}
