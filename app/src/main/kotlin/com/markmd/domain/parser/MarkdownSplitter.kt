package com.markmd.domain.parser

/**
 * A section of the rendered markdown document that corresponds to either
 * a heading (with its body) or the content preceding the first heading.
 */
data class MarkdownSection(
    val anchor: String?,
    val content: String,
)

/**
 * Splits markdown content into sections by top-level headings, keeping each
 * heading together with the body until the next heading. This allows rendering
 * the document in a [androidx.compose.foundation.lazy.LazyColumn] so that
 * clicking a ToC entry can scroll to the matching section index.
 */
fun splitMarkdownByHeadings(content: String): List<MarkdownSection> {
    val matches = TocParser.HEADING_REGEX.findAll(content).toList()
    if (matches.isEmpty()) return listOf(MarkdownSection(anchor = null, content = content))

    val sections = mutableListOf<MarkdownSection>()
    val firstStart = matches.first().range.first
    if (firstStart > 0) {
        val preface = content.substring(0, firstStart)
        if (preface.isNotBlank()) {
            sections.add(MarkdownSection(anchor = null, content = preface))
        }
    }
    for (i in matches.indices) {
        val start = matches[i].range.first
        val end = if (i + 1 < matches.size) matches[i + 1].range.first else content.length
        val title = matches[i].groupValues[2].trim()
        sections.add(
            MarkdownSection(
                anchor = TocParser.generateAnchor(title),
                content = content.substring(start, end),
            )
        )
    }
    return sections
}
