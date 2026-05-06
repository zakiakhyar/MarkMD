package com.markmd.ui.screen.viewer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.markmd.data.model.AppTheme
import com.markmd.domain.parser.MarkdownSection
import com.markmd.domain.parser.splitMarkdownByHeadings
import com.mikepenz.markdown.annotator.annotatorSettings
import com.mikepenz.markdown.annotator.buildMarkdownAnnotatedString
import com.mikepenz.markdown.compose.LocalMarkdownColors
import com.mikepenz.markdown.compose.LocalMarkdownDimens
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownBlockQuote
import com.mikepenz.markdown.compose.elements.MarkdownHeader
import com.mikepenz.markdown.compose.elements.MarkdownTable
import com.mikepenz.markdown.compose.elements.MarkdownText
import com.mikepenz.markdown.compose.elements.material.MarkdownBasicText
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.DefaultMarkdownColors
import com.mikepenz.markdown.model.markdownDimens
import com.mikepenz.markdown.model.markdownPadding
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMElementTypes.HEADER
import org.intellij.markdown.flavours.gfm.GFMElementTypes.ROW
import org.intellij.markdown.flavours.gfm.GFMTokenTypes.CELL
import org.intellij.markdown.flavours.gfm.GFMTokenTypes.TABLE_SEPARATOR

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
    theme: AppTheme = AppTheme.SYSTEM,
    onAnchorClick: (String) -> Unit = {},
    lazyListState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier,
) {
    val gh = rememberMarkdownTokens(theme)

    // ------------------------------------------------------------------
    // Colors — exact GitHub token mapping
    // ------------------------------------------------------------------
    val baseUriHandler = LocalUriHandler.current
    val anchorUriHandler = remember(onAnchorClick, baseUriHandler) {
        object : UriHandler {
            override fun openUri(uri: String) {
                if (uri.startsWith("#")) {
                    onAnchorClick(uri.removePrefix("#"))
                } else {
                    try { baseUriHandler.openUri(uri) } catch (_: Exception) {}
                }
            }
        }
    }

    val colors = markdownColor(
        text                 = gh.text,
        codeText             = gh.codeText,
        inlineCodeText       = gh.codeText,
        linkText             = gh.link,
        codeBackground       = gh.codeBlockBg,
        inlineCodeBackground = gh.inlineCodeBg,
        dividerColor         = gh.divider,
        tableText            = gh.tableText,
        tableBackground      = gh.tableBg,
    )

    // ------------------------------------------------------------------
    // Typography — GitHub em-based scale, monospace for code
    // ------------------------------------------------------------------
    val bodyStyle = TextStyle(
        fontSize     = fontSize.sp,
        fontWeight   = FontWeight.Normal,
        lineHeight   = (fontSize * 1.5f).sp,
        letterSpacing = 0.sp,
        color        = gh.text,
    )
    val monoStyle = TextStyle(
        fontSize     = (fontSize * 0.9f).sp,
        fontFamily   = FontFamily.Monospace,
        fontWeight   = FontWeight.Normal,
        lineHeight   = (fontSize * 1.45f).sp,
        letterSpacing = 0.sp,
        color        = gh.codeText,
    )
    val quoteStyle = TextStyle(
        fontSize     = fontSize.sp,
        fontStyle    = FontStyle.Normal,
        fontWeight   = FontWeight.Normal,
        lineHeight   = (fontSize * 1.5f).sp,
        letterSpacing = 0.sp,
        color        = gh.blockquoteText,
    )

    val typography = markdownTypography(
        text        = bodyStyle,
        paragraph   = bodyStyle,
        code        = monoStyle,
        inlineCode  = monoStyle,
        quote       = quoteStyle,
        bullet      = bodyStyle,
        ordered     = bodyStyle,
        list        = bodyStyle,
        h1 = githubHeadingStyle(1, fontSize, gh.text),
        h2 = githubHeadingStyle(2, fontSize, gh.text),
        h3 = githubHeadingStyle(3, fontSize, gh.text),
        h4 = githubHeadingStyle(4, fontSize, gh.text),
        h5 = githubHeadingStyle(5, fontSize, gh.text),
        h6 = githubHeadingStyle(6, fontSize, gh.textMuted),
    )

    // ------------------------------------------------------------------
    // Dimens — GitHub-like code corner radius, thicker blockquote bar
    // ------------------------------------------------------------------
    val dimens = markdownDimens(
        tableCellWidth           = 160.dp,
        tableCellPadding         = 6.dp,
        blockQuoteThickness      = 4.dp,
        codeBackgroundCornerSize = 6.dp,
        dividerThickness         = 2.dp,
    )

    // ------------------------------------------------------------------
    // Padding — block=0 eliminates double-spacing at section boundaries;
    //           blockquote tuned to match GitHub's 4dp left bar
    // ------------------------------------------------------------------
    val padding = markdownPadding(
        block          = 0.dp,
        blockQuote     = androidx.compose.foundation.layout.PaddingValues(
            start = 0.dp, end = 0.dp, top = 6.dp, bottom = 6.dp
        ),
        blockQuoteText = androidx.compose.foundation.layout.PaddingValues(
            start = 14.dp, top = 2.dp, end = 0.dp, bottom = 2.dp
        ),
        blockQuoteBar  = androidx.compose.foundation.layout.PaddingValues.Absolute(
            left = 2.dp, top = 0.dp, right = 14.dp, bottom = 0.dp
        ),
    )

    // ------------------------------------------------------------------
    // Components — h1/h2 get a bottom border; blockquote uses gh color;
    //              horizontalRule uses gh divider color; table full-wrap
    // ------------------------------------------------------------------
    val borderColor = gh.divider

    val components = markdownComponents(
        // h1 — bottom border; top padding only if not first element (handled by section padding)
        heading1 = { model ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
                    .semantics { heading() },
            ) {
                MarkdownHeader(
                    content = model.content,
                    node    = model.node,
                    style   = model.typography.h1,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(1.dp)
                        .background(borderColor),
                )
            }
        },
        // h2 — bottom border
        heading2 = { model ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 4.dp)
                    .semantics { heading() },
            ) {
                MarkdownHeader(
                    content = model.content,
                    node    = model.node,
                    style   = model.typography.h2,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .height(1.dp)
                        .background(borderColor),
                )
            }
        },
        // h3–h6 — wrapped in Box so we can control vertical padding
        heading3 = { model ->
            Box(modifier = Modifier.padding(top = 14.dp, bottom = 2.dp)) {
                MarkdownHeader(content = model.content, node = model.node, style = model.typography.h3)
            }
        },
        heading4 = { model ->
            Box(modifier = Modifier.padding(top = 12.dp, bottom = 2.dp)) {
                MarkdownHeader(content = model.content, node = model.node, style = model.typography.h4)
            }
        },
        heading5 = { model ->
            Box(modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)) {
                MarkdownHeader(content = model.content, node = model.node, style = model.typography.h5)
            }
        },
        heading6 = { model ->
            Box(modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)) {
                MarkdownHeader(content = model.content, node = model.node, style = model.typography.h6)
            }
        },
        // setext headings — GFM misparsing guard:
        // Paragraphs followed by "---"/"===" without blank line become setext headings.
        // If the setext content is just dashes/equals (no real heading text) or is a
        // blockquote line, render as HR instead to avoid swallowing paragraphs.
        setextHeading1 = { model ->
            val rawNodeText = model.content
                .substring(model.node.startOffset, model.node.endOffset)
                .trimStart()
            val setextContent = model.node.children
                .find { it.type == MarkdownTokenTypes.SETEXT_CONTENT }
                ?.let { model.content.substring(it.startOffset, it.endOffset).trim() }
                .orEmpty()
            val isParsedBlockquote = rawNodeText.startsWith(">")
            val isRealHeading = setextContent.isNotBlank()
                && !setextContent.all { it == '=' || it == '-' || it.isWhitespace() }
            if (!isRealHeading || isParsedBlockquote) {
                // Not a real heading — recover any preceding paragraph text and show HR
                if (isParsedBlockquote) {
                    val quoteText = rawNodeText
                        .lines()
                        .filter { it.trimStart().startsWith(">") }
                        .joinToString("\n") { it.trimStart().removePrefix(">").trimStart() }
                    if (quoteText.isNotBlank()) {
                        MarkdownText(
                            content  = AnnotatedString(quoteText),
                            modifier = Modifier.padding(start = 18.dp),
                            style    = model.typography.quote,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .height(2.dp)
                        .background(gh.divider),
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp)
                        .semantics { heading() },
                ) {
                    MarkdownHeader(
                        content          = model.content,
                        node             = model.node,
                        style            = model.typography.h1,
                        contentChildType = MarkdownTokenTypes.SETEXT_CONTENT,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .height(1.dp)
                            .background(borderColor),
                    )
                }
            }
        },
        setextHeading2 = { model ->
            val rawNodeText2 = model.content
                .substring(model.node.startOffset, model.node.endOffset)
                .trimStart()
            val setextContent2 = model.node.children
                .find { it.type == MarkdownTokenTypes.SETEXT_CONTENT }
                ?.let { model.content.substring(it.startOffset, it.endOffset).trim() }
                .orEmpty()
            val isParsedBlockquote2 = rawNodeText2.startsWith(">")
            val isRealHeading2 = setextContent2.isNotBlank()
                && !setextContent2.all { it == '=' || it == '-' || it.isWhitespace() }
            if (!isRealHeading2 || isParsedBlockquote2) {
                if (isParsedBlockquote2) {
                    val quoteText = rawNodeText2
                        .lines()
                        .filter { it.trimStart().startsWith(">") }
                        .joinToString("\n") { it.trimStart().removePrefix(">").trimStart() }
                    if (quoteText.isNotBlank()) {
                        MarkdownText(
                            content  = AnnotatedString(quoteText),
                            modifier = Modifier.padding(start = 18.dp),
                            style    = model.typography.quote,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .height(2.dp)
                        .background(gh.divider),
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 4.dp)
                        .semantics { heading() },
                ) {
                    MarkdownHeader(
                        content          = model.content,
                        node             = model.node,
                        style            = model.typography.h2,
                        contentChildType = MarkdownTokenTypes.SETEXT_CONTENT,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .height(1.dp)
                            .background(borderColor),
                    )
                }
            }
        },
        // blockquote — MarkdownBlockQuote uses LocalMarkdownColors.text for the bar color.
        // Override text=blockquoteBorder for the bar; paragraph text uses typography.quote.color.
        blockQuote = { model ->
            val currentColors = LocalMarkdownColors.current
            val barColors = DefaultMarkdownColors(
                text                 = gh.blockquoteBorder,
                codeText             = currentColors.codeText,
                inlineCodeText       = currentColors.inlineCodeText,
                linkText             = currentColors.linkText,
                codeBackground       = currentColors.codeBackground,
                inlineCodeBackground = currentColors.inlineCodeBackground,
                dividerColor         = currentColors.dividerColor,
                tableText            = currentColors.tableText,
                tableBackground      = currentColors.tableBackground,
            )
            CompositionLocalProvider(LocalMarkdownColors provides barColors) {
                MarkdownBlockQuote(
                    content = model.content,
                    node    = model.node,
                    style   = model.typography.quote,
                )
            }
        },
        // horizontal rule — GitHub renders as 2dp, slightly padded
        horizontalRule = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .height(2.dp)
                    .background(gh.divider),
            )
        },
        // table — fully custom: always horizontal-scrollable, header centered+bold, rows 1-line
        table = { model ->
            GhTable(
                content = model.content,
                node    = model.node,
                style   = model.typography.text,
                dividerColor = gh.divider,
            )
        },
    )

    CompositionLocalProvider(LocalUriHandler provides anchorUriHandler) {
    SelectionContainer(
        modifier = modifier
            .fillMaxSize()
            .background(gh.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        LazyColumn(state = lazyListState) {
            items(sections) { section ->
                Markdown(
                    content    = section.content,
                    colors     = colors,
                    typography = typography,
                    dimens     = dimens,
                    padding    = padding,
                    components = components,
                    modifier   = Modifier.fillMaxWidth(),
                )
            }
        }
    }
    }
}

// ---------------------------------------------------------------------------
// GitHub-style table — SubcomposeLayout for proper per-column widths
// ---------------------------------------------------------------------------

@Composable
private fun GhTable(
    content: String,
    node: ASTNode,
    style: TextStyle,
    dividerColor: Color,
) {
    val cellPadding = LocalMarkdownDimens.current.tableCellPadding
    val cornerSize  = LocalMarkdownDimens.current.tableCornerSize
    val annotator   = annotatorSettings()
    val bgColor     = LocalMarkdownColors.current.tableBackground
    val cellColor   = LocalMarkdownColors.current.tableText
    val headerStyle = style.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

    // Collect all rows (header + data) as list of list of AnnotatedString
    val headerNode  = node.children.firstOrNull { it.type == HEADER }
    val dataRows    = node.children.filter { it.type == ROW }
    val colCount    = headerNode?.children?.count { it.type == CELL } ?: 0

    if (colCount == 0) return

    // Build annotated strings for every cell
    val headerCells: List<AnnotatedString> = headerNode
        ?.children?.filter { it.type == CELL }
        ?.map { content.buildMarkdownAnnotatedString(it, headerStyle, annotator) }
        ?: emptyList()

    val dataRowCells: List<List<AnnotatedString>> = dataRows.map { row ->
        row.children.filter { it.type == CELL }
            .map { content.buildMarkdownAnnotatedString(it, style, annotator) }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(cornerSize))
    ) {
        SubcomposeLayout(
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) { constraints ->
            val unconstrained = constraints.copy(minWidth = 0, maxWidth = Int.MAX_VALUE)

            // --- Pass 1: measure each cell unconstrained to get natural width ---
            val colWidths = IntArray(colCount) { 0 }

            // measure header cells
            headerCells.forEachIndexed { col, text ->
                val p = subcompose("hm_$col") {
                    MarkdownBasicText(
                        text = text, style = headerStyle, color = cellColor,
                        maxLines = 1, softWrap = false,
                        modifier = Modifier.padding(cellPadding),
                    )
                }[0].measure(unconstrained)
                if (p.width > colWidths[col]) colWidths[col] = p.width
            }

            // measure data row cells
            dataRowCells.forEachIndexed { row, cells ->
                cells.forEachIndexed { col, text ->
                    if (col < colCount) {
                        val p = subcompose("dm_${row}_$col") {
                            MarkdownBasicText(
                                text = text, style = style, color = cellColor,
                                maxLines = 1, softWrap = false,
                                modifier = Modifier.padding(cellPadding),
                            )
                        }[0].measure(unconstrained)
                        if (p.width > colWidths[col]) colWidths[col] = p.width
                    }
                }
            }

            val totalWidth  = colWidths.sum()
            val dividerH    = 1
            val rowCount    = 1 + dataRows.size       // header + data rows
            val separatorH  = dividerH                // between header and data

            // --- Pass 2: place all rows with fixed column widths ---
            var yOffset = 0
            val placeables = mutableListOf<Pair<androidx.compose.ui.layout.Placeable, Int>>()

            // header row
            val headerRowH = headerCells.indices.maxOfOrNull { col ->
                subcompose("h_$col") {
                    MarkdownBasicText(
                        text = headerCells[col], style = headerStyle, color = cellColor,
                        textAlign = TextAlign.Center, maxLines = 1, softWrap = false,
                        modifier = Modifier.padding(cellPadding),
                    )
                }[0].measure(constraints.copy(
                    minWidth = colWidths[col], maxWidth = colWidths[col], minHeight = 0
                )).height
            } ?: 0

            var headerXOffset = 0
            headerCells.indices.forEach { col ->
                val p = subcompose("hr_$col") {
                    MarkdownBasicText(
                        text = headerCells[col], style = headerStyle, color = cellColor,
                        textAlign = TextAlign.Center, maxLines = 1, softWrap = false,
                        modifier = Modifier.padding(cellPadding),
                    )
                }[0].measure(constraints.copy(
                    minWidth = colWidths[col], maxWidth = colWidths[col],
                    minHeight = headerRowH, maxHeight = headerRowH,
                ))
                placeables += Pair(p, headerXOffset)
                headerXOffset += colWidths[col]
            }
            yOffset += headerRowH

            // separator after header
            val sepP = subcompose("sep") {
                Box(Modifier.height(1.dp).background(dividerColor).fillMaxWidth())
            }[0].measure(constraints.copy(
                minWidth = totalWidth, maxWidth = totalWidth, minHeight = dividerH, maxHeight = dividerH
            ))
            placeables += Pair(sepP, 0)
            yOffset += separatorH

            // data rows
            dataRowCells.forEachIndexed { rowIdx, cells ->
                val rowH = cells.indices.maxOfOrNull { col ->
                    if (col >= colCount) return@maxOfOrNull 0
                    subcompose("rwh_${rowIdx}_$col") {
                        MarkdownBasicText(
                            text = cells[col], style = style, color = cellColor,
                            maxLines = 1, softWrap = false,
                            modifier = Modifier.padding(cellPadding),
                        )
                    }[0].measure(constraints.copy(
                        minWidth = colWidths[col], maxWidth = colWidths[col], minHeight = 0
                    )).height
                } ?: 0

                var xOffset = 0
                cells.forEachIndexed { col, text ->
                    if (col < colCount) {
                        val p = subcompose("rw_${rowIdx}_$col") {
                            MarkdownBasicText(
                                text = text, style = style, color = cellColor,
                                maxLines = 1, softWrap = false,
                                modifier = Modifier.padding(cellPadding),
                            )
                        }[0].measure(constraints.copy(
                            minWidth = colWidths[col], maxWidth = colWidths[col],
                            minHeight = rowH, maxHeight = rowH,
                        ))
                        placeables += Pair(p, xOffset + yOffset.let { 0 })
                        xOffset += colWidths[col]
                    }
                }

                // row divider
                val rowDiv = subcompose("rdiv_$rowIdx") {
                    Box(Modifier.height(1.dp).background(dividerColor.copy(alpha = 0.35f)))
                }[0].measure(constraints.copy(
                    minWidth = totalWidth, maxWidth = totalWidth, minHeight = dividerH, maxHeight = dividerH
                ))
                placeables += Pair(rowDiv, 0)

                yOffset += rowH + dividerH
            }

            val totalHeight = yOffset

            layout(totalWidth, totalHeight) {
                // Re-place in correct pass with correct y; use fresh subcompose for placement
                var y = 0

                // header
                var x = 0
                headerCells.indices.forEach { col ->
                    val p = subcompose("place_h_$col") {
                        MarkdownBasicText(
                            text = headerCells[col], style = headerStyle, color = cellColor,
                            textAlign = TextAlign.Center, maxLines = 1, softWrap = false,
                            modifier = Modifier.padding(cellPadding),
                        )
                    }[0].measure(constraints.copy(
                        minWidth = colWidths[col], maxWidth = colWidths[col],
                        minHeight = headerRowH, maxHeight = headerRowH,
                    ))
                    p.place(x, y)
                    x += colWidths[col]
                }
                y += headerRowH

                // separator
                subcompose("place_sep") {
                    Box(Modifier.height(1.dp).background(dividerColor))
                }[0].measure(constraints.copy(
                    minWidth = totalWidth, maxWidth = totalWidth, minHeight = dividerH, maxHeight = dividerH
                )).place(0, y)
                y += dividerH

                // data rows
                dataRowCells.forEachIndexed { rowIdx, cells ->
                    val rowH = cells.indices.maxOfOrNull { col ->
                        if (col >= colCount) return@maxOfOrNull 0
                        subcompose("place_rwh_${rowIdx}_$col") {
                            MarkdownBasicText(
                                text = cells[col], style = style, color = cellColor,
                                maxLines = 1, softWrap = false,
                                modifier = Modifier.padding(cellPadding),
                            )
                        }[0].measure(constraints.copy(
                            minWidth = colWidths[col], maxWidth = colWidths[col], minHeight = 0
                        )).height
                    } ?: 0

                    x = 0
                    cells.forEachIndexed { col, text ->
                        if (col < colCount) {
                            subcompose("place_rw_${rowIdx}_$col") {
                                MarkdownBasicText(
                                    text = text, style = style, color = cellColor,
                                    maxLines = 1, softWrap = false,
                                    modifier = Modifier.padding(cellPadding),
                                )
                            }[0].measure(constraints.copy(
                                minWidth = colWidths[col], maxWidth = colWidths[col],
                                minHeight = rowH, maxHeight = rowH,
                            )).place(x, y)
                            x += colWidths[col]
                        }
                    }
                    y += rowH

                    subcompose("place_rdiv_$rowIdx") {
                        Box(Modifier.height(1.dp).background(dividerColor.copy(alpha = 0.35f)))
                    }[0].measure(constraints.copy(
                        minWidth = totalWidth, maxWidth = totalWidth, minHeight = dividerH, maxHeight = dividerH
                    )).place(0, y)
                    y += dividerH
                }
            }
        }
    }
}
