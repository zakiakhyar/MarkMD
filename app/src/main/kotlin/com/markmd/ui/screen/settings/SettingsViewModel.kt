package com.markmd.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.markmd.data.model.AppTheme
import com.markmd.data.model.FontFamily
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
        settingsRepository.fontSize,
        settingsRepository.fontFamily,
        settingsRepository.keepScreenOn,
        settingsRepository.showLineNumbers
    ) { theme, fontSize, fontFamily, keepScreenOn, showLineNumbers ->
        SettingsUiState(
            theme = theme,
            fontSize = fontSize,
            fontFamily = fontFamily,
            keepScreenOn = keepScreenOn,
            showLineNumbers = showLineNumbers
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

    fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setKeepScreenOn(enabled)
        }
    }

    fun setShowLineNumbers(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShowLineNumbers(enabled)
        }
    }
}

data class SettingsUiState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val fontSize: Int = 16,
    val fontFamily: FontFamily = FontFamily.SANS_SERIF,
    val keepScreenOn: Boolean = false,
    val showLineNumbers: Boolean = false
)
