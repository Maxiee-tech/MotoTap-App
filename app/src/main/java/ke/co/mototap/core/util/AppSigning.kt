package ke.co.mototap.core.util

import android.content.pm.PackageManager
import android.os.Build
import com.google.firebase.FirebaseApp
import java.security.MessageDigest

object AppSigning {
    fun sha1(): String = digest("SHA-1")

    fun sha256(): String = digest("SHA-256")

    private fun digest(algorithm: String): String {
        return try {
            val context = FirebaseApp.getInstance().applicationContext
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val info = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES,
                )
                val signingInfo = info.signingInfo ?: return ""
                if (signingInfo.hasMultipleSigners()) {
                    signingInfo.apkContentsSigners
                } else {
                    signingInfo.signingCertificateHistory
                }
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES,
                ).signatures
            }
            val cert = signatures?.firstOrNull()?.toByteArray() ?: return ""
            MessageDigest.getInstance(algorithm).digest(cert)
                .joinToString(":") { byte -> "%02X".format(byte) }
        } catch (_: Exception) {
            ""
        }
    }
}
