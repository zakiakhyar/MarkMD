package com.markmd.ui.screen.viewer.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.markmd.data.model.AppTheme

@Composable
fun ReaderSettingsSheetContent(
    fontSize: Int,
    theme: AppTheme,
    onFontSizeChange: (Int) -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Reader Settings",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp),
        )

        // Font size
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Font size: ${fontSize}sp",
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(
                    onClick = { onFontSizeChange((fontSize - 1).coerceAtLeast(10)) },
                    enabled = fontSize > 10,
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease font size")
                }
                Slider(
                    value = fontSize.toFloat(),
                    onValueChange = { onFontSizeChange(it.toInt()) },
                    valueRange = 10f..32f,
                    steps = 21,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = { onFontSizeChange((fontSize + 1).coerceAtMost(32)) },
                    enabled = fontSize < 32,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase font size")
                }
            }
        }

        // Theme
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            ) {
                AppTheme.entries.forEach { t ->
                    FilterChip(
                        selected = t == theme,
                        onClick = { onThemeChange(t) },
                        label = { Text(t.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
            }
        }
    }
}
