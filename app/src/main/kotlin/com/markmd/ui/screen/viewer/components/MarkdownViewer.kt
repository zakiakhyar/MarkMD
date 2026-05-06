package com.markmd.ui.screen.viewer.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.markmd.domain.parser.MarkdownSection
import com.markmd.domain.parser.splitMarkdownByHeadings
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownTable
import com.mikepenz.markdown.compose.elements.MarkdownTableHeader
import com.mikepenz.markdown.compose.elements.MarkdownTableRow
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.markdownDimens

/**
 * Produces the list of sections from markdown content. Exposed so the caller
 * (e.g. [com.markmd.ui.screen.viewer.ViewerScreen]) can resolve a ToC anchor
 * to a LazyColumn item index to scroll to.
 */
@Composable
fun rememberMarkdownSections(content: String): List<MarkdownSection> =
    remember(content) { splitMarkdownByHeadings(content) }

@Composable
fun MarkdownViewer(
    sections: List<MarkdownSection>,
    fontSize: Int,
    lazyListState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier,
) {
    val colors = markdownColor(
        text = MaterialTheme.colorScheme.onBackground,
        codeText = MaterialTheme.colorScheme.onSurfaceVariant,
        codeBackground = MaterialTheme.colorScheme.surfaceVariant,
        linkText = MaterialTheme.colorScheme.primary,
        tableBackground = MaterialTheme.colorScheme.surfaceVariant,
        tableText = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    val typography = markdownTypography(
        text = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp),
        code = MaterialTheme.typography.bodyMedium.copy(fontSize = (fontSize - 1).sp),
        paragraph = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp),
        h1 = MaterialTheme.typography.headlineLarge.copy(fontSize = (fontSize + 16).sp),
        h2 = MaterialTheme.typography.headlineMedium.copy(fontSize = (fontSize + 12).sp),
        h3 = MaterialTheme.typography.headlineSmall.copy(fontSize = (fontSize + 8).sp),
        h4 = MaterialTheme.typography.titleLarge.copy(fontSize = (fontSize + 4).sp),
        h5 = MaterialTheme.typography.titleMedium.copy(fontSize = (fontSize + 2).sp),
        h6 = MaterialTheme.typography.titleSmall.copy(fontSize = fontSize.sp),
    )
    val dimens = markdownDimens(tableCellWidth = 200.dp)
    val components = markdownComponents(
        table = { model ->
            MarkdownTable(
                content = model.content,
                node = model.node,
                style = model.typography.text,
                headerBlock = { c, header, tableWidth, style ->
                    MarkdownTableHeader(
                        content = c,
                        header = header,
                        tableWidth = tableWidth,
                        style = style,
                        maxLines = Int.MAX_VALUE,
                        overflow = TextOverflow.Clip,
                    )
                },
                rowBlock = { c, row, tableWidth, style ->
                    MarkdownTableRow(
                        content = c,
                        header = row,
                        tableWidth = tableWidth,
                        style = style,
                        maxLines = Int.MAX_VALUE,
                        overflow = TextOverflow.Clip,
                    )
                },
            )
        },
    )

    LazyColumn(
        state = lazyListState,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        items(sections) { section ->
            Markdown(
                content = section.content,
                colors = colors,
                typography = typography,
                dimens = dimens,
                components = components,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

