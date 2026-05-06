package com.markmd.ui.screen.editor

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markmd.domain.usecase.ReadFileUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val readFile: ReadFileUseCase
) : ViewModel() {

    private val _events = MutableSharedFlow<EditorEvent>()
    val events: SharedFlow<EditorEvent> = _events.asSharedFlow()

    fun loadDocument(uri: Uri) {
        viewModelScope.launch {
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
            // TODO: Implement save functionality
            _events.emit(EditorEvent.Saved)
        }
    }
}

sealed class EditorEvent {
    data class DocumentLoaded(val content: String) : EditorEvent()
    data object Saved : EditorEvent()
    data class Error(val message: String) : EditorEvent()
}
