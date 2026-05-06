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
 * Normalizes raw markdown so the intellij-markdown parser handles edge cases correctly:
 * - Ensures `---`/`===` HR lines are surrounded by blank lines (prevents setext mis-parse).
 * - Ensures table rows (`|…|`) that immediately follow a non-blank, non-table line get a
 *   blank line inserted before them (prevents GFM table-in-list-continuation mis-parse).
 */
fun normalizeMarkdown(content: String): String {
    val lines = content.lines()
    val out = ArrayList<String>(lines.size + 8)
    for (i in lines.indices) {
        val line = lines[i]
        val trimmed = line.trim()

        val isHr = trimmed.matches(Regex("^(-{3,}|={3,}|\\*{3,})$"))
        val isTableRow = trimmed.startsWith("|")
        val prevTrimmed = if (i > 0) lines[i - 1].trim() else ""
        val prevIsBlank = prevTrimmed.isEmpty()

        if (isHr && !prevIsBlank) {
            out.add("")
        } else if (isTableRow && !prevIsBlank && !prevTrimmed.startsWith("|")) {
            out.add("")
        }

        out.add(line)

        if (isHr) {
            val nextTrimmed = if (i + 1 < lines.size) lines[i + 1].trim() else ""
            if (nextTrimmed.isNotEmpty()) out.add("")
        }
    }
    return out.joinToString("\n")
}

/**
 * Splits markdown content into sections by top-level headings, keeping each
 * heading together with the body until the next heading. This allows rendering
 * the document in a [androidx.compose.foundation.lazy.LazyColumn] so that
 * clicking a ToC entry can scroll to the matching section index.
 */
fun splitMarkdownByHeadings(content: String): List<MarkdownSection> {
    val normalized = normalizeMarkdown(content)

    // Build a set of character ranges that are inside fenced code blocks (``` or ~~~).
    // Heading matches that fall inside these ranges are skipped.
    val fenceRegex = Regex("""^(`{3,}|~{3,}).*$""", RegexOption.MULTILINE)
    val fencedRanges = mutableListOf<IntRange>()
    var fenceStart: Int? = null
    var fenceMarker: String? = null
    for (fm in fenceRegex.findAll(normalized)) {
        val marker = fm.groupValues[1]
        if (fenceStart == null) {
            fenceStart = fm.range.first
            fenceMarker = marker
        } else if (fenceMarker != null && marker.startsWith(fenceMarker!![0]) && marker.length >= fenceMarker!!.length) {
            fencedRanges += fenceStart!!..fm.range.last
            fenceStart = null
            fenceMarker = null
        }
    }

    val allMatches = TocParser.HEADING_REGEX.findAll(normalized).toList()
    val matches = allMatches.filter { m ->
        fencedRanges.none { range -> m.range.first in range }
    }
    if (matches.isEmpty()) return listOf(MarkdownSection(anchor = null, content = normalized))

    val sections = mutableListOf<MarkdownSection>()
    val firstStart = matches.first().range.first
    if (firstStart > 0) {
        val preface = normalized.substring(0, firstStart)
        if (preface.isNotBlank()) {
            sections.add(MarkdownSection(anchor = null, content = preface))
        }
    }
    for (i in matches.indices) {
        val start = matches[i].range.first
        val end = if (i + 1 < matches.size) matches[i + 1].range.first else normalized.length
        val title = matches[i].groupValues[2].trim()
        sections.add(
            MarkdownSection(
                anchor = TocParser.generateAnchor(title),
                content = normalized.substring(start, end),
            )
        )
    }
    return sections
}
