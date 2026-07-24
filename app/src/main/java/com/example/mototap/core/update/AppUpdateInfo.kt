package com.example.mototap.core.update

/**
 * Remote app version metadata (hosted as `/app-version.json` on the website).
 * Bump [versionCode] whenever you publish a new APK, matching `app/build.gradle.kts`.
 */
data class AppUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val forceUpdate: Boolean = false,
    val changelog: String = "",
)
