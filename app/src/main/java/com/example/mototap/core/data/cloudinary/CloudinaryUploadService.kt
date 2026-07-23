package com.example.mototap.core.data.cloudinary

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Unsigned Cloudinary uploads — same paths and presets as mototap_web (Option B).
 */
object CloudinaryUploadService {

    suspend fun uploadSignupImage(
        context: Context,
        userId: String,
        folder: String,
        uri: Uri,
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val category = CloudinaryConfig.categoryForFolder(folder)
            val preset = CloudinaryConfig.presetForCategory(category)
            val cloudName = CloudinaryConfig.cloudName
            if (cloudName.isBlank() || preset.isBlank()) {
                error("Cloudinary is not configured in the app.")
            }

            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val extension = MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(mimeType)
                ?.takeIf { it.isNotBlank() }
                ?: "jpg"
            val fileName = "upload_${System.currentTimeMillis()}.$extension"
            val resourceType = CloudinaryConfig.resourceType(category)
            val folderPath = CloudinaryConfig.storagePath(userId, folder)
            val boundary = "----MotoTap${UUID.randomUUID()}"

            val url = URL("https://api.cloudinary.com/v1_1/$cloudName/$resourceType/upload")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doInput = true
                doOutput = true
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            }

            DataOutputStream(connection.outputStream).use { out ->
                fun writeField(name: String, value: String) {
                    out.writeBytes("--$boundary\r\n")
                    out.writeBytes("Content-Disposition: form-data; name=\"$name\"\r\n\r\n")
                    out.writeBytes(value)
                    out.writeBytes("\r\n")
                }

                writeField("upload_preset", preset)
                writeField("folder", folderPath)
                writeField(
                    "tags",
                    "uid:$userId,stage:signup,doc_type:$folder,preset:$category"
                )

                out.writeBytes("--$boundary\r\n")
                out.writeBytes(
                    "Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"\r\n"
                )
                out.writeBytes("Content-Type: $mimeType\r\n\r\n")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    input.copyTo(out)
                } ?: error("Could not read selected file.")
                out.writeBytes("\r\n")
                out.writeBytes("--$boundary--\r\n")
                out.flush()
            }

            val responseCode = connection.responseCode
            val body = (if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            })?.bufferedReader()?.use { it.readText() }.orEmpty()

            if (responseCode !in 200..299) {
                val message = runCatching {
                    JSONObject(body).optJSONObject("error")?.optString("message")
                }.getOrNull()?.takeIf { it.isNotBlank() }
                    ?: "Upload failed ($responseCode)."
                error(message)
            }

            val secureUrl = JSONObject(body).optString("secure_url")
            if (secureUrl.isBlank()) error("Upload succeeded but no URL was returned.")
            secureUrl
        }
    }
}
