package com.markmd.data.repository

import android.content.Context
import android.net.Uri
import com.markmd.data.local.db.AppDatabase
import com.markmd.data.local.db.DocumentEntity
import com.markmd.data.model.Document
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase
) {
    private val documentDao = database.documentDao()

    private val _fileSaved = MutableSharedFlow<Uri>(extraBufferCapacity = 1)
    val fileSaved: SharedFlow<Uri> = _fileSaved.asSharedFlow()

    suspend fun notifyFileSaved(uri: Uri) {
        _fileSaved.emit(uri)
    }

    fun getRecentDocuments(): Flow<List<Document>> {
        return documentDao.getRecentDocuments().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun getDocument(uri: Uri): Document? {
        return documentDao.getDocument(uri.toString())?.toDomainModel()
    }

    suspend fun readDocumentContent(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                val sb = StringBuilder()
                val buf = CharArray(8192)
                val reader = InputStreamReader(stream, Charsets.UTF_8)
                var total = 0
                var read: Int
                while (reader.read(buf).also { read = it } != -1) {
                    total += read
                    if (total > 10 * 1024 * 1024) {
                        return@withContext Result.failure(
                            Exception("File too large to display (>10 MB)")
                        )
                    }
                    sb.append(buf, 0, read)
                }
                sb.toString()
            } ?: return@withContext Result.failure(Exception("Failed to open file"))

            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveDocument(document: Document) {
        val existing = documentDao.getDocument(document.uri.toString())
        val entity = document.toEntity().copy(isPinned = existing?.isPinned ?: false)
        documentDao.insertDocument(entity)
    }

    suspend fun updateScrollPosition(uri: Uri, position: Int) {
        documentDao.updateScrollPosition(uri.toString(), position)
    }

    suspend fun deleteDocument(uri: Uri) {
        documentDao.deleteDocument(uri.toString())
    }

    suspend fun togglePin(uri: Uri) {
        documentDao.togglePin(uri.toString())
    }

    private fun DocumentEntity.toDomainModel(): Document {
        return Document(
            uri = Uri.parse(uri),
            title = title,
            content = cachedContent ?: "",
            lastModified = lastOpened,
            scrollPosition = scrollPosition,
            isPinned = isPinned,
        )
    }

    private fun Document.toEntity(): DocumentEntity {
        return DocumentEntity(
            uri = uri.toString(),
            title = title,
            lastOpened = lastModified,
            scrollPosition = scrollPosition,
            cachedContent = content,
            isPinned = isPinned,
        )
    }
}
