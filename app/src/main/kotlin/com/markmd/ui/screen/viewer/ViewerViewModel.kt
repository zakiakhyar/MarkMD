package com.markmd.ui.screen.viewer

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markmd.data.model.AppTheme
import com.markmd.data.model.Document
import com.markmd.data.model.TocEntry
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
    }

    fun loadDocument(uri: Uri) {
        if (currentUri == uri && _uiState.value.document != null) return

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
}

data class ViewerUiState(
    val document: Document? = null,
    val toc: List<TocEntry> = emptyList(),
    val isLoading: Boolean = false,
    val isTocVisible: Boolean = false,
    val error: String? = null
)

data class ReaderSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val fontSize: Int = 16,
)

sealed class ViewerEvent {
    data class ScrollToAnchor(val anchor: String) : ViewerEvent()
    data class NavigateToEditor(val uri: Uri) : ViewerEvent()
    data object NavigateToSettings : ViewerEvent()
    data class ShareDocument(val uri: Uri) : ViewerEvent()
    data object ExportPdf : ViewerEvent()
}
