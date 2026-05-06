package com.markmd.data.repository

import com.markmd.data.local.datastore.SettingsDataStore
import com.markmd.data.model.AppTheme
import com.markmd.data.model.FontFamily
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: SettingsDataStore
) {
    val theme: Flow<AppTheme> = dataStore.theme
    val fontSize: Flow<Int> = dataStore.fontSize
    val fontFamily: Flow<FontFamily> = dataStore.fontFamily
    val keepScreenOn: Flow<Boolean> = dataStore.keepScreenOn
    val showLineNumbers: Flow<Boolean> = dataStore.showLineNumbers

    suspend fun setTheme(theme: AppTheme) = dataStore.setTheme(theme)
    suspend fun setFontSize(size: Int) = dataStore.setFontSize(size)
    suspend fun setFontFamily(family: FontFamily) = dataStore.setFontFamily(family)
    suspend fun setKeepScreenOn(enabled: Boolean) = dataStore.setKeepScreenOn(enabled)
    suspend fun setShowLineNumbers(enabled: Boolean) = dataStore.setShowLineNumbers(enabled)
}
