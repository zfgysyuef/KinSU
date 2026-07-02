plugins {
    alias(libs.plugins.agp.app) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.compose.compiler) apply false
}

val androidMinSdkVersion by extra(31)
val androidTargetSdkVersion by extra(37)
val androidCompileSdkVersion by extra(37)
val androidCompileSdkVersionMinor by extra(0)
val androidBuildToolsVersion by extra("37.0.0")
val androidCompileNdkVersion: String by extra(libs.versions.ndk.get())
val androidSourceCompatibility by extra(JavaVersion.VERSION_17)
val androidTargetCompatibility by extra(JavaVersion.VERSION_17)
val managerVersionCode by extra(getVersionCode())
val managerVersionName by extra(getVersionName())

fun getGitCommitCount(): Int {
    val process = Runtime.getRuntime().exec(
        arrayOf("git", "rev-list", "--count", "HEAD"),
        null,
        rootDir.parentFile
    )
    return process.inputStream.bufferedReader().use {
        it.readText().trim().toIntOrNull() ?: 0
    }
}

fun getGitDescribe(): String {
    val process = Runtime.getRuntime().exec(
        arrayOf("git", "describe", "--tags", "--always"),
        null,
        rootDir.parentFile
    )
    return process.inputStream.bufferedReader().use {
        it.readText().trim().ifBlank { "unknown" }
    }
}

fun getVersionCode(): Int {
    return 30039
}

fun getVersionName(): String {
    return "3.1.11"
}
