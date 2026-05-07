package com.markmd.domain.usecase

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class ImportUrlUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend operator fun invoke(urlString: String): Result<Pair<Uri, String>> = withContext(Dispatchers.IO) {
        try {
            val url = URL(urlString.trim())
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 30_000
                requestMethod = "GET"
                setRequestProperty("Accept", "text/plain, text/markdown, */*")
            }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                return@withContext Result.failure(Exception("HTTP $responseCode: ${connection.responseMessage}"))
            }

            val content = connection.inputStream.use { stream ->
                val sb = StringBuilder()
                val buf = CharArray(8192)
                val reader = stream.reader(Charsets.UTF_8)
                var total = 0
                var read: Int
                while (reader.read(buf).also { read = it } != -1) {
                    total += read
                    if (total > 10 * 1024 * 1024) {
                        return@withContext Result.failure(Exception("File too large to import (>10 MB)"))
                    }
                    sb.append(buf, 0, read)
                }
                sb.toString()
            }
            connection.disconnect()

            val fileName = url.path.substringAfterLast('/').let {
                if (it.isBlank() || !it.contains('.')) "imported_${System.currentTimeMillis()}.md" else it
            }
            val file = File(context.filesDir, fileName)
            file.writeText(content, Charsets.UTF_8)

            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            Result.success(Pair(uri, content))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
