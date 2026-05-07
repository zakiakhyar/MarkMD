package com.markmd.ui.screen.editor

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markmd.domain.usecase.ReadFileUseCase
import com.markmd.domain.usecase.WriteFileUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val readFile: ReadFileUseCase,
    private val writeFile: WriteFileUseCase,
    private val contentResolver: ContentResolver,
) : ViewModel() {

    private val _events = MutableSharedFlow<EditorEvent>()
    val events: SharedFlow<EditorEvent> = _events.asSharedFlow()

    fun loadDocument(uri: Uri) {
        viewModelScope.launch {
            takePersistablePermission(uri)
            readFile(uri)
                .onSuccess { document ->
                    _events.emit(EditorEvent.DocumentLoaded(document.content))
                }
                .onFailure { error ->
                    _events.emit(EditorEvent.Error(error.message ?: "Failed to load document"))
                }
        }
    }

    fun saveDocument(uri: Uri, content: String) {
        viewModelScope.launch {
            takePersistablePermission(uri)
            writeFile(uri, content)
                .onSuccess {
                    _events.emit(EditorEvent.Saved)
                }
                .onFailure { error ->
                    _events.emit(EditorEvent.Error(error.message ?: "Failed to save document"))
                }
        }
    }

    private fun takePersistablePermission(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (_: SecurityException) {}
    }
}

sealed class EditorEvent {
    data class DocumentLoaded(val content: String) : EditorEvent()
    data object Saved : EditorEvent()
    data class Error(val message: String) : EditorEvent()
}
