package com.example.finditcampus.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

object ImageUploader {
    suspend fun uploadToCatbox(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val boundary = "Boundary-" + UUID.randomUUID().toString()
            val url = URL("https://catbox.moe/user/api.php")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary)

            val outputStream = connection.outputStream
            val writer = PrintWriter(OutputStreamWriter(outputStream, "UTF-8"), true)

            // Add reqtype
            writer.append("--" + boundary + "\r\n")
            writer.append("Content-Disposition: form-data; name=\"reqtype\"\r\n\r\n")
            writer.append("fileupload\r\n")

            // Add file
            val fileName = getFileName(context, uri) ?: "upload.jpg"
            writer.append("--" + boundary + "\r\n")
            writer.append("Content-Disposition: form-data; name=\"fileToUpload\"; filename=\"" + fileName + "\"\r\n")
            writer.append("Content-Type: image/jpeg\r\n\r\n")
            writer.flush()

            // Copy file data
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                outputStream.flush()
            }

            writer.append("\r\n--" + boundary + "--\r\n")
            writer.close()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return@withContext connection.inputStream.bufferedReader().use { it.readText() }.trim()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = cursor.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path?.let { path ->
                val cut = path.lastIndexOf('/')
                if (cut != -1) path.substring(cut + 1) else path
            }
        }
        return result
    }
}
