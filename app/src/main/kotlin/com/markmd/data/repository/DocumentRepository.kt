package com.markmd.data.repository

import android.content.Context
import android.net.Uri
import com.markmd.data.local.db.AppDatabase
import com.markmd.data.local.db.DocumentEntity
import com.markmd.data.model.Document
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
                BufferedReader(InputStreamReader(stream)).readText()
            } ?: return@withContext Result.failure(Exception("Failed to open input stream"))

            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveDocument(document: Document) {
        documentDao.insertDocument(document.toEntity())
    }

    suspend fun updateScrollPosition(uri: Uri, position: Int) {
        documentDao.updateScrollPosition(uri.toString(), position)
    }

    suspend fun deleteDocument(uri: Uri) {
        documentDao.deleteDocument(uri.toString())
    }

    private fun DocumentEntity.toDomainModel(): Document {
        return Document(
            uri = Uri.parse(uri),
            title = title,
            content = cachedContent ?: "",
            lastModified = lastOpened,
            scrollPosition = scrollPosition
        )
    }

    private fun Document.toEntity(): DocumentEntity {
        return DocumentEntity(
            uri = uri.toString(),
            title = title,
            lastOpened = lastModified,
            scrollPosition = scrollPosition,
            cachedContent = content
        )
    }
}
