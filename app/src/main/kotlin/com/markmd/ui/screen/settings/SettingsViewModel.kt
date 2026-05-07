package com.markmd.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markmd.data.model.AppTheme
import com.markmd.data.model.FontFamily
import com.markmd.data.model.ReadingTheme
import com.markmd.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.theme,
        settingsRepository.readingTheme,
        settingsRepository.fontSize,
        settingsRepository.fontFamily,
    ) { theme, readingTheme, fontSize, fontFamily ->
        SettingsUiState(
            theme = theme,
            readingTheme = readingTheme,
            fontSize = fontSize,
            fontFamily = fontFamily,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
        }
    }

    fun setReadingTheme(theme: ReadingTheme) {
        viewModelScope.launch {
            settingsRepository.setReadingTheme(theme)
        }
    }

    fun setFontSize(size: Int) {
        viewModelScope.launch {
            settingsRepository.setFontSize(size)
        }
    }

    fun setFontFamily(family: FontFamily) {
        viewModelScope.launch {
            settingsRepository.setFontFamily(family)
        }
    }

}

data class SettingsUiState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val readingTheme: ReadingTheme = ReadingTheme.SYSTEM,
    val fontSize: Int = 16,
    val fontFamily: FontFamily = FontFamily.SYSTEM_DEFAULT,
)
