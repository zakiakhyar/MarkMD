package com.markmd.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.markmd.R
import com.markmd.data.model.AppTheme
import com.markmd.data.model.FontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showFontFamilyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Appearance Section
            SettingsSection(title = stringResource(R.string.settings_appearance)) {
                SettingsItem(
                    title = stringResource(R.string.settings_theme),
                    value = getThemeLabel(uiState.theme),
                    onClick = { showThemeDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                SettingsItem(
                    title = stringResource(R.string.settings_font_family),
                    value = getFontFamilyLabel(uiState.fontFamily),
                    onClick = { showFontFamilyDialog = true }
                )
            }

            // Font Size Section
            SettingsSection(title = stringResource(R.string.settings_font_size)) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "${uiState.fontSize}sp",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = uiState.fontSize.toFloat(),
                        onValueChange = { viewModel.setFontSize(it.toInt()) },
                        valueRange = 12f..24f,
                        steps = 11
                    )
                }
            }

        }
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.settings_theme)) },
            text = {
                Column {
                    AppTheme.entries.forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setTheme(theme)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.theme == theme,
                                onClick = {
                                    viewModel.setTheme(theme)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(getThemeLabel(theme))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    // Font Family Selection Dialog
    if (showFontFamilyDialog) {
        AlertDialog(
            onDismissRequest = { showFontFamilyDialog = false },
            title = { Text(stringResource(R.string.settings_font_family)) },
            text = {
                Column {
                    FontFamily.entries.forEach { family ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setFontFamily(family)
                                    showFontFamilyDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.fontFamily == family,
                                onClick = {
                                    viewModel.setFontFamily(family)
                                    showFontFamilyDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(getFontFamilyLabel(family))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFontFamilyDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        content()
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SettingsItem(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun getThemeLabel(theme: AppTheme): String {
    return when (theme) {
        AppTheme.SYSTEM    -> stringResource(R.string.theme_system)
        AppTheme.LIGHT     -> stringResource(R.string.theme_light)
        AppTheme.DARK      -> stringResource(R.string.theme_dark)
        AppTheme.SEPIA     -> stringResource(R.string.theme_sepia)
        AppTheme.AMOLED    -> stringResource(R.string.theme_amoled)
        AppTheme.DARK_BLUE -> stringResource(R.string.theme_dark_blue)
    }
}

@Composable
private fun getFontFamilyLabel(family: FontFamily): String {
    return when (family) {
        FontFamily.SANS_SERIF -> stringResource(R.string.font_sans)
        FontFamily.SERIF -> stringResource(R.string.font_serif)
        FontFamily.MONOSPACE -> stringResource(R.string.font_mono)
    }
}
