package com.markmd.ui.screen.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.markmd.R
import com.markmd.data.model.AppTheme
import com.markmd.data.model.ReadingTheme
import com.markmd.data.model.FontFamily as AppFontFamily
import com.markmd.ui.screen.viewer.components.MarkdownPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showFontFamilyDialog by remember { mutableStateOf(false) }
    var showFeaturesDialog by remember { mutableStateOf(false) }

    fun openUrl(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // ── APPEARANCE ──────────────────────────────────────────────
            SettingsSectionLabel(stringResource(R.string.settings_appearance))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SettingsIconBox(icon = Icons.Default.Palette, tint = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.settings_app_theme), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    }
                    // Segmented theme toggle: System / Light / Dark
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        listOf(AppTheme.SYSTEM to "System", AppTheme.LIGHT to "Light", AppTheme.DARK to "Dark").forEach { (t, label) ->
                            val selected = uiState.theme == t
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).clickable { viewModel.setTheme(t) },
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp),
                                )
                            }
                        }
                    }
                }
            }

            // ── READING ─────────────────────────────────────────────────
            SettingsSectionLabel(stringResource(R.string.settings_reading))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    // Font Family row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showFontFamilyDialog = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        SettingsIconBox(icon = Icons.Default.TextFields, tint = Color(0xFF4CAF50))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.settings_font_family), style = MaterialTheme.typography.bodyLarge)
                        }
                        Text(
                            text = getFontFamilyLabel(uiState.fontFamily),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Reading Theme chips
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SettingsIconBox(icon = Icons.Default.Book, tint = Color(0xFFFF9800))
                            Text(stringResource(R.string.settings_reading_theme), style = MaterialTheme.typography.bodyLarge)
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        ) {
                            ReadingTheme.entries.forEach { t ->
                                ReadingThemeChip(
                                    label = readingThemeShortLabel(t),
                                    selected = uiState.readingTheme == t,
                                    bgColor = readingThemePreviewBg(t),
                                    textColor = readingThemePreviewText(t),
                                    onClick = { viewModel.setReadingTheme(t) },
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Font Size
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SettingsIconBox(icon = Icons.Default.FormatSize, tint = Color(0xFFFF5722))
                            Column {
                                Text(stringResource(R.string.settings_font_size), style = MaterialTheme.typography.bodyLarge)
                                Text("${uiState.fontSize} pt", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("A", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Slider(
                                value = uiState.fontSize.toFloat(),
                                onValueChange = { viewModel.setFontSize(it.toInt()) },
                                valueRange = 12f..28f,
                                steps = 15,
                                modifier = Modifier.weight(1f),
                            )
                            Text("A", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        // Preview box — reflects selected reading theme + font
                        val previewPalette = readingThemePalette(uiState.readingTheme)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(previewPalette.background)
                                .padding(16.dp),
                        ) {
                            Text(
                                text = "The quick brown fox jumps over the lazy dog. A beautifully typeset reading experience makes all the difference.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = uiState.fontSize.sp,
                                    lineHeight = (uiState.fontSize * 1.5f).sp,
                                    fontFamily = when (uiState.fontFamily) {
                                        AppFontFamily.SERIF, AppFontFamily.GEORGIA, AppFontFamily.LITERATA -> FontFamily.Serif
                                        AppFontFamily.SOURCE_CODE_PRO -> FontFamily.Monospace
                                        else -> FontFamily.Default
                                    },
                                ),
                                color = previewPalette.text,
                            )
                        }
                    }
                }
            }

            // ── ABOUT ────────────────────────────────────────────────────
            SettingsSectionLabel(stringResource(R.string.settings_about))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    // App identity row — tap to show features
                    Row(
                        modifier = Modifier
                            .clickable { showFeaturesDialog = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        SettingsIconBox(icon = Icons.Default.Book, tint = MaterialTheme.colorScheme.primary)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.about_app_name), style = MaterialTheme.typography.bodyLarge)
                            Text(stringResource(R.string.about_tagline), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    AboutRow(icon = Icons.Default.Info, tint = Color(0xFF2196F3), title = stringResource(R.string.about_version), subtitle = stringResource(R.string.about_version_value), hasArrow = false)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    AboutRow(icon = Icons.Default.Info, tint = Color(0xFF9C27B0), title = stringResource(R.string.about_website), subtitle = stringResource(R.string.about_website_url), hasArrow = true, onClick = { openUrl(context.getString(R.string.about_website_full_url)) })
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    AboutRow(icon = Icons.Default.Policy, tint = Color(0xFF4CAF50), title = stringResource(R.string.about_privacy), subtitle = stringResource(R.string.about_privacy_sub), hasArrow = true, onClick = { openUrl(context.getString(R.string.about_privacy_url)) })
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    AboutRow(icon = Icons.Default.Book, tint = Color(0xFFFF9800), title = stringResource(R.string.about_terms), subtitle = stringResource(R.string.about_terms_sub), hasArrow = true, onClick = { openUrl(context.getString(R.string.about_terms_url)) })
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // Features Dialog
    if (showFeaturesDialog) {
        AlertDialog(
            onDismissRequest = { showFeaturesDialog = false },
            title = { Text(stringResource(R.string.about_features_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val features = listOf(
                        "📖" to "Read Markdown files with beautiful rendering",
                        "✏️" to "Edit files with syntax-aware editor",
                        "🎨" to "8 reading themes: Light, Dark, Sepia, AMOLED, Blue, Green, Solar & Auto",
                        "🔤" to "5 font choices: System, Lora, Merriweather, Literata, Atkinson Hyperlegible, Source Code Pro",
                        "🔍" to "Full-text search with match navigation",
                        "📋" to "Table of Contents for quick navigation",
                        "🔗" to "Import from URL or paste from clipboard",
                        "📤" to "Share documents with other apps",
                        "🌙" to "Focus mode for distraction-free reading",
                        "📌" to "Pin frequently-used documents",
                        "🗂️" to "Recent documents history",
                    )
                    features.forEach { (emoji, desc) ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(emoji, style = MaterialTheme.typography.bodyMedium)
                            Text(desc, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFeaturesDialog = false }) { Text(stringResource(R.string.action_ok)) }
            }
        )
    }

    // Font Family Dialog
    if (showFontFamilyDialog) {
        AlertDialog(
            onDismissRequest = { showFontFamilyDialog = false },
            title = { Text(stringResource(R.string.settings_font_family)) },
            text = {
                Column {
                    AppFontFamily.entries.forEach { family ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setFontFamily(family); showFontFamilyDialog = false }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = uiState.fontFamily == family, onClick = { viewModel.setFontFamily(family); showFontFamilyDialog = false })
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(getFontFamilyLabel(family), style = MaterialTheme.typography.bodyLarge)
                                Text(getFontFamilySubtitle(family), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFontFamilyDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

@Composable
private fun SettingsIconBox(icon: ImageVector, tint: Color) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(tint.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun ReadingThemeChip(label: String, selected: Boolean, bgColor: Color, textColor: Color, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .size(width = 64.dp, height = 72.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = "Aa", style = MaterialTheme.typography.titleMedium.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal), color = textColor)
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.8f),
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp),
            )
        }
    }
}

@Composable
private fun AboutRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String,
    hasArrow: Boolean,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SettingsIconBox(icon = icon, tint = tint)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (hasArrow) Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun readingThemeShortLabel(theme: ReadingTheme) = when (theme) {
    ReadingTheme.SYSTEM     -> "Auto"
    ReadingTheme.LIGHT      -> "Light"
    ReadingTheme.DARK       -> "Dark"
    ReadingTheme.SEPIA      -> "Sepia"
    ReadingTheme.AMOLED     -> "AMOLED"
    ReadingTheme.DARK_BLUE  -> "Blue"
    ReadingTheme.DARK_GREEN -> "Green"
    ReadingTheme.SOLARIZED  -> "Solar"
}

private fun readingThemePalette(theme: ReadingTheme) = when (theme) {
    ReadingTheme.SYSTEM     -> MarkdownPalette.light
    ReadingTheme.LIGHT      -> MarkdownPalette.light
    ReadingTheme.DARK       -> MarkdownPalette.dark
    ReadingTheme.SEPIA      -> MarkdownPalette.sepia
    ReadingTheme.AMOLED     -> MarkdownPalette.amoled
    ReadingTheme.DARK_BLUE  -> MarkdownPalette.darkBlue
    ReadingTheme.DARK_GREEN -> MarkdownPalette.darkGreen
    ReadingTheme.SOLARIZED  -> MarkdownPalette.solarized
}

private fun readingThemePreviewBg(theme: ReadingTheme): Color = readingThemePalette(theme).background
private fun readingThemePreviewText(theme: ReadingTheme): Color = readingThemePalette(theme).text

@Composable
private fun getFontFamilyLabel(family: AppFontFamily): String = when (family) {
    AppFontFamily.SYSTEM_DEFAULT  -> stringResource(R.string.font_system_default)
    AppFontFamily.SERIF           -> stringResource(R.string.font_serif)
    AppFontFamily.GEORGIA         -> stringResource(R.string.font_georgia)
    AppFontFamily.LITERATA        -> stringResource(R.string.font_literata)
    AppFontFamily.OPEN_DYSLEXIC   -> stringResource(R.string.font_open_dyslexic)
    AppFontFamily.SOURCE_CODE_PRO -> stringResource(R.string.font_source_code_pro)
}

private fun getFontFamilySubtitle(family: AppFontFamily): String = when (family) {
    AppFontFamily.SYSTEM_DEFAULT  -> "Default system font"
    AppFontFamily.SERIF           -> "Classic reading, newspapers"
    AppFontFamily.GEORGIA         -> "Elegant, long-form articles"
    AppFontFamily.LITERATA        -> "Designed for e-readers"
    AppFontFamily.OPEN_DYSLEXIC   -> "Accessibility-focused"
    AppFontFamily.SOURCE_CODE_PRO -> "Monospace, developer docs"
}
