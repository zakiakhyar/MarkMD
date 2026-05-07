package com.markmd.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.markmd.data.model.AppTheme
import com.markmd.data.model.FontFamily
import com.markmd.data.model.ReadingTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme")
        private val READING_THEME_KEY = stringPreferencesKey("reading_theme")
        private val FONT_SIZE_KEY = intPreferencesKey("font_size")
        private val FONT_FAMILY_KEY = stringPreferencesKey("font_family")
        private val KEEP_SCREEN_ON_KEY = booleanPreferencesKey("keep_screen_on")
        private val SHOW_LINE_NUMBERS_KEY = booleanPreferencesKey("show_line_numbers")

        private const val DEFAULT_FONT_SIZE = 14
    }

    val theme: Flow<AppTheme> = dataStore.data.map { prefs ->
        prefs[THEME_KEY]?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() } ?: AppTheme.SYSTEM
    }

    val readingTheme: Flow<ReadingTheme> = dataStore.data.map { prefs ->
        prefs[READING_THEME_KEY]?.let { runCatching { ReadingTheme.valueOf(it) }.getOrNull() } ?: ReadingTheme.SYSTEM
    }

    val fontSize: Flow<Int> = dataStore.data.map { prefs ->
        prefs[FONT_SIZE_KEY] ?: DEFAULT_FONT_SIZE
    }

    val fontFamily: Flow<FontFamily> = dataStore.data.map { prefs ->
        prefs[FONT_FAMILY_KEY]?.let { runCatching { FontFamily.valueOf(it) }.getOrNull() } ?: FontFamily.SYSTEM_DEFAULT
    }

    val keepScreenOn: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEEP_SCREEN_ON_KEY] ?: false
    }

    val showLineNumbers: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[SHOW_LINE_NUMBERS_KEY] ?: false
    }

    suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { it[THEME_KEY] = theme.name }
    }

    suspend fun setReadingTheme(theme: ReadingTheme) {
        dataStore.edit { it[READING_THEME_KEY] = theme.name }
    }

    suspend fun setFontSize(size: Int) {
        dataStore.edit { it[FONT_SIZE_KEY] = size }
    }

    suspend fun setFontFamily(family: FontFamily) {
        dataStore.edit { it[FONT_FAMILY_KEY] = family.name }
    }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        dataStore.edit { it[KEEP_SCREEN_ON_KEY] = enabled }
    }

    suspend fun setShowLineNumbers(enabled: Boolean) {
        dataStore.edit { it[SHOW_LINE_NUMBERS_KEY] = enabled }
    }
}
