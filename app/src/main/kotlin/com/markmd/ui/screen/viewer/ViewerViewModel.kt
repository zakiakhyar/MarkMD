package com.markmd.ui.screen.viewer

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markmd.data.model.AppTheme
import com.markmd.data.model.Document
import com.markmd.data.model.TocEntry
import com.markmd.data.repository.DocumentRepository
import com.markmd.data.repository.SettingsRepository
import com.markmd.domain.usecase.ParseTocUseCase
import com.markmd.domain.usecase.ReadFileUseCase
import com.markmd.domain.usecase.SaveProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val readFile: ReadFileUseCase,
    private val parseToc: ParseTocUseCase,
    private val saveProgress: SaveProgressUseCase,
    private val settingsRepository: SettingsRepository,
    private val documentRepository: DocumentRepository,
) : ViewModel() {

    val readerSettings: StateFlow<ReaderSettings> = combine(
        settingsRepository.theme,
        settingsRepository.fontSize,
    ) { theme, fontSize -> ReaderSettings(theme = theme, fontSize = fontSize) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ReaderSettings(),
        )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { settingsRepository.setTheme(theme) }
    }

    fun setFontSize(size: Int) {
        viewModelScope.launch { settingsRepository.setFontSize(size.coerceIn(10, 32)) }
    }

    private val _uiState = MutableStateFlow(ViewerUiState())
    val uiState: StateFlow<ViewerUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ViewerEvent>()
    val events: SharedFlow<ViewerEvent> = _events.asSharedFlow()

    private var currentUri: Uri? = null

    init {
        // Try to get URI from saved state (if process death)
        savedStateHandle.get<String>("uri")?.let { uriString ->
            loadDocument(Uri.parse(uriString))
        }
        // Auto-reload when editor saves the current file
        viewModelScope.launch {
            documentRepository.fileSaved.collect { savedUri ->
                if (savedUri == currentUri) {
                    loadDocumentInternal(savedUri)
                }
            }
        }
    }

    fun loadDocument(uri: Uri) {
        if (currentUri == uri && _uiState.value.document != null) return
        loadDocumentInternal(uri)
    }

    fun reloadDocument() {
        currentUri?.let { loadDocumentInternal(it) }
    }

    private fun loadDocumentInternal(uri: Uri) {
        currentUri = uri
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            readFile(uri)
                .onSuccess { document ->
                    val toc = parseToc(document.content)
                    _uiState.update {
                        it.copy(
                            document = document,
                            toc = toc,
                            isLoading = false
                        )
                    }
                    if (_uiState.value.searchQuery.isNotEmpty()) {
                        updateSearchMatches(document.content, _uiState.value.searchQuery)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load document"
                        )
                    }
                }
        }
    }

    fun saveScrollPosition(position: Int) {
        currentUri?.let { uri ->
            viewModelScope.launch {
                saveProgress(uri, position)
            }
        }
    }

    fun onTocClick(anchor: String) {
        viewModelScope.launch {
            _events.emit(ViewerEvent.ScrollToAnchor(anchor))
        }
    }

    fun onEditClick() {
        currentUri?.let { uri ->
            viewModelScope.launch {
                _events.emit(ViewerEvent.NavigateToEditor(uri))
            }
        }
    }

    fun onSettingsClick() {
        viewModelScope.launch {
            _events.emit(ViewerEvent.NavigateToSettings)
        }
    }

    fun onShareClick() {
        currentUri?.let { uri ->
            viewModelScope.launch {
                _events.emit(ViewerEvent.ShareDocument(uri))
            }
        }
    }

    fun onExportPdfClick() {
        viewModelScope.launch {
            _events.emit(ViewerEvent.ExportPdf)
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        val content = _uiState.value.document?.content ?: return
        updateSearchMatches(content, query)
    }

    fun onSearchNext() {
        val state = _uiState.value
        if (state.searchMatches.isEmpty()) return
        val next = (state.currentMatchIndex + 1) % state.searchMatches.size
        _uiState.update { it.copy(currentMatchIndex = next) }
        viewModelScope.launch {
            _events.emit(ViewerEvent.ScrollToMatch(state.searchMatches[next]))
        }
    }

    fun onSearchPrev() {
        val state = _uiState.value
        if (state.searchMatches.isEmpty()) return
        val prev = if (state.currentMatchIndex <= 0) state.searchMatches.size - 1
                   else state.currentMatchIndex - 1
        _uiState.update { it.copy(currentMatchIndex = prev) }
        viewModelScope.launch {
            _events.emit(ViewerEvent.ScrollToMatch(state.searchMatches[prev]))
        }
    }

    fun onSearchClose() {
        _uiState.update { it.copy(searchQuery = "", searchMatches = emptyList(), currentMatchIndex = -1) }
    }

    private fun updateSearchMatches(content: String, query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(searchMatches = emptyList(), currentMatchIndex = -1) }
            return
        }
        val matches = mutableListOf<Int>()
        var idx = 0
        val lower = content.lowercase()
        val lowerQ = query.lowercase()
        while (idx <= lower.length - lowerQ.length) {
            val found = lower.indexOf(lowerQ, idx)
            if (found < 0) break
            matches += found
            idx = found + 1
        }
        val newIndex = if (matches.isEmpty()) -1 else 0
        _uiState.update { it.copy(searchMatches = matches, currentMatchIndex = newIndex) }
        if (matches.isNotEmpty()) {
            viewModelScope.launch {
                _events.emit(ViewerEvent.ScrollToMatch(matches[0]))
            }
        }
    }
}

data class ViewerUiState(
    val document: Document? = null,
    val toc: List<TocEntry> = emptyList(),
    val isLoading: Boolean = false,
    val isTocVisible: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val searchMatches: List<Int> = emptyList(),
    val currentMatchIndex: Int = -1,
) {
    val matchCount get() = searchMatches.size
    val currentMatch get() = if (currentMatchIndex >= 0 && searchMatches.isNotEmpty()) currentMatchIndex + 1 else 0
}

data class ReaderSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val fontSize: Int = 14,
)

sealed class ViewerEvent {
    data class ScrollToAnchor(val anchor: String) : ViewerEvent()
    data class NavigateToEditor(val uri: Uri) : ViewerEvent()
    data object NavigateToSettings : ViewerEvent()
    data class ShareDocument(val uri: Uri) : ViewerEvent()
    data object ExportPdf : ViewerEvent()
    data class ScrollToMatch(val charOffset: Int) : ViewerEvent()
}
