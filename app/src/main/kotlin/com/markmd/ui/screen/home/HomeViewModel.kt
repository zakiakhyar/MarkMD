package com.markmd.ui.screen.home

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markmd.data.model.Document
import com.markmd.data.repository.DocumentRepository
import com.markmd.domain.usecase.GetRecentDocumentsUseCase
import com.markmd.domain.usecase.ImportUrlUseCase
import com.markmd.domain.usecase.ReadFileUseCase
import com.markmd.domain.usecase.SaveClipboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRecentDocuments: GetRecentDocumentsUseCase,
    private val readFile: ReadFileUseCase,
    private val repository: DocumentRepository,
    private val contentResolver: ContentResolver,
    private val importUrl: ImportUrlUseCase,
    private val saveClipboard: SaveClipboardUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    init {
        loadRecentDocuments()
    }

    private fun loadRecentDocuments() {
        getRecentDocuments()
            .onEach { documents ->
                _uiState.value = _uiState.value.copy(recentDocuments = documents, isLoading = false)
            }
            .launchIn(viewModelScope)
    }

    fun onFileSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            takePersistablePermission(uri)

            readFile(uri)
                .onSuccess { document ->
                    repository.saveDocument(document)
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(HomeEvent.NavigateToViewer(document.uri))
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(HomeEvent.ShowError(error.message ?: "Error opening file"))
                }
        }
    }

    fun onRecentDocumentClick(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            takePersistablePermission(uri)

            readFile(uri)
                .onSuccess { document ->
                    repository.saveDocument(document)
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(HomeEvent.NavigateToViewer(document.uri))
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(HomeEvent.ShowError(error.message ?: "Error opening file"))
                }
        }
    }

    fun onImportUrl(urlString: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            importUrl(urlString)
                .onSuccess { (uri, content) ->
                    val title = urlString.substringAfterLast('/').substringBefore('?')
                        .let { if (it.isBlank()) "Imported Document" else it }
                    val document = com.markmd.data.model.Document(
                        uri = uri,
                        title = title,
                        content = content,
                        lastModified = System.currentTimeMillis(),
                    )
                    repository.saveDocument(document)
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(HomeEvent.NavigateToViewer(uri))
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(HomeEvent.ShowError(error.message ?: "Error importing URL"))
                }
        }
    }

    fun onPasteClipboard(content: String, fileName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            saveClipboard(content, fileName)
                .onSuccess { (uri, text) ->
                    val title = fileName.trim().let {
                        if (it.isBlank()) "Clipboard Document" else it.removeSuffix(".md")
                    }
                    val document = com.markmd.data.model.Document(
                        uri = uri,
                        title = title,
                        content = text,
                        lastModified = System.currentTimeMillis(),
                    )
                    repository.saveDocument(document)
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(HomeEvent.NavigateToViewer(uri))
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(HomeEvent.ShowError(error.message ?: "Error saving clipboard content"))
                }
        }
    }

    fun onSaveToLocal(uri: Uri, sourceUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val sourceContent = repository.readDocumentContent(sourceUri).getOrThrow()
                contentResolver.openOutputStream(uri, "wt")?.use { stream ->
                    stream.write(sourceContent.toByteArray(Charsets.UTF_8))
                }
                takePersistablePermission(uri)
                readFile(uri)
                    .onSuccess { document ->
                        repository.saveDocument(document)
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        _events.emit(HomeEvent.ShowMessage("Saved to local storage"))
                        _events.emit(HomeEvent.NavigateToViewer(uri))
                    }
                    .onFailure {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        _events.emit(HomeEvent.ShowError("Error reading saved file"))
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _events.emit(HomeEvent.ShowError(e.message ?: "Error saving file"))
            }
        }
    }

    fun setPendingSaveUri(uri: Uri) {
        _uiState.value = _uiState.value.copy(pendingSaveUri = uri)
    }

    fun onDeleteDocument(uri: Uri) {
        viewModelScope.launch {
            repository.deleteDocument(uri)
        }
    }

    fun onSettingsClick() {
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToSettings)
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

data class HomeUiState(
    val recentDocuments: List<Document> = emptyList(),
    val isLoading: Boolean = false,
    val pendingSaveUri: Uri? = null,
)

sealed class HomeEvent {
    data class NavigateToViewer(val uri: Uri) : HomeEvent()
    data object NavigateToSettings : HomeEvent()
    data class ShowError(val message: String) : HomeEvent()
    data class ShowMessage(val message: String) : HomeEvent()
}
