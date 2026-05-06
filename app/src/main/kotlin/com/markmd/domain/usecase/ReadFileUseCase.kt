package com.markmd.domain.usecase

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import com.markmd.data.model.Document
import com.markmd.data.repository.DocumentRepository
import javax.inject.Inject

class ReadFileUseCase @Inject constructor(
    private val repository: DocumentRepository,
    private val contentResolver: ContentResolver
) {
    suspend operator fun invoke(uri: Uri): Result<Document> {
        val title = getFileName(uri)

        return repository.readDocumentContent(uri).map { content ->
            Document(
                uri = uri,
                title = title,
                content = content
            )
        }
    }

    private fun getFileName(uri: Uri): String {
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex >= 0) {
                        return cursor.getString(displayNameIndex) ?: "Untitled.md"
                    }
                }
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/') ?: "Untitled.md"
    }
}
