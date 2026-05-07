package com.markmd.domain.usecase

import android.content.ContentResolver
import android.net.Uri
import com.markmd.data.repository.DocumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WriteFileUseCase @Inject constructor(
    private val contentResolver: ContentResolver,
    private val repository: DocumentRepository,
) {
    suspend operator fun invoke(uri: Uri, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            contentResolver.openOutputStream(uri, "wt")?.use { stream ->
                stream.write(content.toByteArray(Charsets.UTF_8))
            } ?: return@withContext Result.failure(Exception("Cannot open output stream for $uri"))
            repository.notifyFileSaved(uri)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
