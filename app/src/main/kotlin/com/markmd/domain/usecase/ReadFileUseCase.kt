package com.markmd.domain.usecase

import android.net.Uri
import com.markmd.data.model.Document
import com.markmd.data.repository.DocumentRepository
import javax.inject.Inject

class ReadFileUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    suspend operator fun invoke(uri: Uri): Result<Document> {
        val title = uri.lastPathSegment?.substringAfterLast('/') ?: "Untitled.md"

        return repository.readDocumentContent(uri).map { content ->
            Document(
                uri = uri,
                title = title,
                content = content
            )
        }
    }
}
