package com.example.mototap.core.update

import android.content.Context
import android.util.Log
import com.example.mototap.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object AppUpdateChecker {
    private const val TAG = "AppUpdateChecker"
    private const val PREFS = "mototap_app_update"
    private const val KEY_SNOOZE_UNTIL = "snooze_until_millis"
    private const val SNOOZE_DURATION_MS = 24L * 60L * 60L * 1000L

    /** Prefer production site, then Firebase hosting, then the Cloudflare APK worker. */
    private val VERSION_URLS = listOf(
        "https://mototap.co.ke/app-version.json",
        "https://mototap-447fe.web.app/app-version.json",
        "https://restless-math-2997.maxmasha0.workers.dev/app-version.json",
    )

    /**
     * Returns update info when a newer [versionCode] is published and the user
     * is not within a "Later" snooze window (unless [AppUpdateInfo.forceUpdate]).
     */
    suspend fun checkForUpdate(context: Context): AppUpdateInfo? = withContext(Dispatchers.IO) {
        val remote = fetchRemoteVersion() ?: return@withContext null
        if (remote.versionCode <= BuildConfig.VERSION_CODE) {
            Log.d(TAG, "Up to date (local=${BuildConfig.VERSION_CODE}, remote=${remote.versionCode})")
            return@withContext null
        }
        if (!remote.forceUpdate && isSnoozed(context)) {
            Log.d(TAG, "Update available but snoozed")
            return@withContext null
        }
        remote
    }

    fun snooze(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_SNOOZE_UNTIL, System.currentTimeMillis() + SNOOZE_DURATION_MS)
            .apply()
    }

    private fun isSnoozed(context: Context): Boolean {
        val until = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_SNOOZE_UNTIL, 0L)
        return until > System.currentTimeMillis()
    }

    private fun fetchRemoteVersion(): AppUpdateInfo? {
        for (url in VERSION_URLS) {
            runCatching { fetchFromUrl(url) }.getOrNull()?.let { return it }
        }
        Log.w(TAG, "Could not fetch app-version.json from any host")
        return null
    }

    private fun fetchFromUrl(urlString: String): AppUpdateInfo {
        val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 8_000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
            // Avoid stale CDN/proxy copies of the version file.
            useCaches = false
            setRequestProperty("Cache-Control", "no-cache")
        }
        try {
            val code = connection.responseCode
            require(code in 200..299) { "HTTP $code from $urlString" }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val versionCode = json.getInt("versionCode")
            val apkUrl = json.getString("apkUrl").trim()
            require(versionCode > 0 && apkUrl.startsWith("http")) {
                "Invalid app-version.json"
            }
            return AppUpdateInfo(
                versionCode = versionCode,
                versionName = json.optString("versionName", versionCode.toString()),
                apkUrl = apkUrl,
                forceUpdate = json.optBoolean("forceUpdate", false),
                changelog = json.optString("changelog", "").trim(),
            )
        } finally {
            connection.disconnect()
        }
    }
}
