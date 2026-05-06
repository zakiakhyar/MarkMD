package com.markmd.domain.usecase

import android.net.Uri
import com.markmd.data.repository.DocumentRepository
import javax.inject.Inject

class SaveProgressUseCase @Inject constructor(
    private val repository: DocumentRepository
) {
    suspend operator fun invoke(uri: Uri, scrollPosition: Int) {
        repository.updateScrollPosition(uri, scrollPosition)
    }
}
