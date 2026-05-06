package com.markmd.domain.usecase

import com.markmd.data.model.Document
import com.markmd.data.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentDocumentsUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    operator fun invoke(): Flow<List<Document>> {
        return repository.getRecentDocuments()
    }
}
