package com.markmd.domain.usecase

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class SaveClipboardUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend operator fun invoke(content: String, fileName: String): Result<Pair<Uri, String>> = withContext(Dispatchers.IO) {
        try {
            val safeName = fileName.trim().let {
                if (it.isBlank()) "clipboard_${System.currentTimeMillis()}.md"
                else if (!it.endsWith(".md")) "$it.md"
                else it
            }
            val file = File(context.filesDir, safeName)
            file.writeText(content, Charsets.UTF_8)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            Result.success(Pair(uri, content))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
