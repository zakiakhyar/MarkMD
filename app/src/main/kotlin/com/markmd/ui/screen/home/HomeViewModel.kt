package com.markmd.ui.screen.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markmd.data.model.Document
import com.markmd.domain.usecase.GetRecentDocumentsUseCase
import com.markmd.domain.usecase.ReadFileUseCase
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
    private val readFile: ReadFileUseCase
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
                _uiState.value = _uiState.value.copy(recentDocuments = documents)
            }
            .launchIn(viewModelScope)
    }

    fun onFileSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            readFile(uri)
                .onSuccess { document ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(HomeEvent.NavigateToViewer(document.uri))
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(HomeEvent.ShowError(error.message ?: "Error opening file"))
                }
        }
    }

    fun onSettingsClick() {
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToSettings)
        }
    }
}

data class HomeUiState(
    val recentDocuments: List<Document> = emptyList(),
    val isLoading: Boolean = false
)

sealed class HomeEvent {
    data class NavigateToViewer(val uri: Uri) : HomeEvent()
    data object NavigateToSettings : HomeEvent()
    data class ShowError(val message: String) : HomeEvent()
}
